package tacky

import assembly.AllocateStack
import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Call
import assembly.Cdq
import assembly.Cmp
import assembly.ConditionCode
import assembly.DeAllocateStack
import assembly.HardwareRegister
import assembly.Idiv
import assembly.Imm
import assembly.Instruction
import assembly.Jmp
import assembly.JmpCC
import assembly.Label
import assembly.Mov
import assembly.Operand
import assembly.Pseudo
import assembly.Push
import assembly.Register
import assembly.Ret
import assembly.SetCC
import assembly.Stack

class TackyToAsm {
    fun convert(tackyProgram: TackyProgram): AsmProgram {
        val asmFunction = tackyProgram.functions.map { convertFunction(it) }

        return AsmProgram(asmFunction)
    }

    private fun convertFunction(tackyFunc: TackyFunction): AsmFunction {
        val paramSetupInstructions = generateParamSetup(tackyFunc.args, tackyFunc.sourceId)
        // Convert the rest of the body as before
        val bodyInstructions = tackyFunc.body.flatMap { convertInstruction(it) }
        return AsmFunction(tackyFunc.name, paramSetupInstructions + bodyInstructions)
    }

    private fun generateParamSetup(params: List<String>, sourceId: String): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val argRegisters =
            listOf(
                HardwareRegister.EDI,
                HardwareRegister.ESI,
                HardwareRegister.EDX,
                HardwareRegister.ECX,
                HardwareRegister.R8D,
                HardwareRegister.R9D
            )

        params.forEachIndexed { index, paramName ->
            if (index < argRegisters.size) {
                val srcReg = Register(argRegisters[index])
                val destPseudo = Pseudo(paramName)
                instructions.add(Mov(srcReg, destPseudo, sourceId))
            } else {
                val stackOffset = 16 + (index - argRegisters.size) * 8
                val srcStack = Stack(stackOffset)
                val destPseudo = Pseudo(paramName)
                instructions.add(Mov(srcStack, destPseudo, sourceId))
            }
        }
        return instructions
    }

    private fun convertInstruction(tackyInstr: TackyInstruction): List<Instruction> =
        when (tackyInstr) {
            is TackyRet -> {
                listOf(
                    Mov(convertVal(tackyInstr.value), Register(HardwareRegister.EAX), tackyInstr.sourceId),
                    Ret(tackyInstr.sourceId)
                )
            }
            is TackyUnary ->
                when (tackyInstr.operator) {
                    TackyUnaryOP.NOT -> {
                        val src = convertVal(tackyInstr.src)
                        val dest = convertVal(tackyInstr.dest)
                        listOf(
                            Cmp(Imm(0), src, tackyInstr.sourceId),
                            Mov(Imm(0), dest, tackyInstr.sourceId), // Zero out destination
                            SetCC(ConditionCode.E, dest, tackyInstr.sourceId) // Set if equal to zero
                        )
                    }
                    else -> {
                        val destOperand = convertVal(tackyInstr.dest)
                        listOf(
                            Mov(convertVal(tackyInstr.src), destOperand, tackyInstr.sourceId),
                            AsmUnary(convertOp(tackyInstr.operator), destOperand, tackyInstr.sourceId)
                        )
                    }
                }

            is TackyBinary -> {
                val src1 = convertVal(tackyInstr.src1)
                val src2 = convertVal(tackyInstr.src2)
                val dest = convertVal(tackyInstr.dest)

                when (tackyInstr.operator) {
                    TackyBinaryOP.ADD ->
                        listOf(
                            Mov(src1, dest, tackyInstr.sourceId),
                            AsmBinary(AsmBinaryOp.ADD, src2, dest, tackyInstr.sourceId)
                        )
                    TackyBinaryOP.MULTIPLY ->
                        listOf(
                            Mov(src1, dest, tackyInstr.sourceId),
                            AsmBinary(AsmBinaryOp.MUL, src2, dest, tackyInstr.sourceId)
                        )
                    TackyBinaryOP.SUBTRACT ->
                        listOf(
                            Mov(src1, dest, tackyInstr.sourceId),
                            AsmBinary(AsmBinaryOp.SUB, src2, dest, tackyInstr.sourceId)
                        )
                    TackyBinaryOP.DIVIDE ->
                        listOf(
                            Mov(src1, Register(HardwareRegister.EAX), tackyInstr.sourceId), // Dividend in EAX
                            Cdq(tackyInstr.sourceId),
                            Idiv(src2, tackyInstr.sourceId), // Divisor
                            Mov(Register(HardwareRegister.EAX), dest, tackyInstr.sourceId) // Quotient result is in EAX
                        )
                    TackyBinaryOP.REMAINDER ->
                        listOf(
                            Mov(src1, Register(HardwareRegister.EAX), tackyInstr.sourceId),
                            Cdq(tackyInstr.sourceId),
                            Idiv(src2, tackyInstr.sourceId),
                            Mov(Register(HardwareRegister.EDX), dest, tackyInstr.sourceId) // Remainder result is in EDX
                        )

                    TackyBinaryOP.EQUAL, TackyBinaryOP.NOT_EQUAL, TackyBinaryOP.GREATER,
                    TackyBinaryOP.GREATER_EQUAL, TackyBinaryOP.LESS, TackyBinaryOP.LESS_EQUAL -> {
                        val condition =
                            when (tackyInstr.operator) {
                                TackyBinaryOP.EQUAL -> ConditionCode.E
                                TackyBinaryOP.NOT_EQUAL -> ConditionCode.NE
                                TackyBinaryOP.GREATER -> ConditionCode.G
                                TackyBinaryOP.GREATER_EQUAL -> ConditionCode.GE
                                TackyBinaryOP.LESS -> ConditionCode.L
                                TackyBinaryOP.LESS_EQUAL -> ConditionCode.LE
                                else -> throw IllegalStateException("Unreachable: This case is logically impossible.")
                            }
                        listOf(
                            Cmp(src2, src1, tackyInstr.sourceId),
                            Mov(Imm(0), dest, tackyInstr.sourceId), // Zero out destination
                            SetCC(condition, dest, tackyInstr.sourceId) // conditionally set the low byte to 0/1
                        )
                    }
                }
            }

            is JumpIfZero -> {
                val condition = convertVal(tackyInstr.condition)
                val target = Label(tackyInstr.target.name, tackyInstr.sourceId)
                listOf(
                    Cmp(Imm(0), condition, tackyInstr.sourceId),
                    JmpCC(ConditionCode.E, target, tackyInstr.sourceId)
                )
            }
            is JumpIfNotZero -> {
                val condition = convertVal(tackyInstr.condition)
                val target = Label(tackyInstr.target.name, tackyInstr.sourceId)
                listOf(
                    Cmp(Imm(0), condition, tackyInstr.sourceId),
                    JmpCC(ConditionCode.NE, target, tackyInstr.sourceId)
                )
            }
            is TackyCopy -> listOf(Mov(convertVal(tackyInstr.src), convertVal(tackyInstr.dest), tackyInstr.sourceId))

            is TackyJump -> listOf(Jmp(Label(tackyInstr.target.name, tackyInstr.sourceId), tackyInstr.sourceId))

            is TackyLabel -> listOf(Label(tackyInstr.name, ""))

            is TackyFunCall -> {
                val instructions = mutableListOf<Instruction>()
                val argRegisters =
                    listOf(
                        HardwareRegister.EDI,
                        HardwareRegister.ESI,
                        HardwareRegister.EDX,
                        HardwareRegister.ECX,
                        HardwareRegister.R8D,
                        HardwareRegister.R9D
                    )

                val registerArgs = tackyInstr.args.take(6)
                val stackArgs = tackyInstr.args.drop(6)

                // Adjust stack alignment
                val stackPadding = if (stackArgs.size % 2 != 0) 8 else 0
                if (stackPadding > 0) {
                    instructions.add(AllocateStack(stackPadding, tackyInstr.sourceId))
                }

                // Pass arguments on the stack in reverse order
                stackArgs.asReversed().forEach { arg ->
                    val asmArg = convertVal(arg)
                    if (asmArg is Stack) {
                        instructions.add(Mov(asmArg, Register(HardwareRegister.EAX), tackyInstr.sourceId))
                        instructions.add(Push(Register(HardwareRegister.EAX), tackyInstr.sourceId))
                    } else {
                        instructions.add(Push(asmArg, tackyInstr.sourceId))
                    }
                }

                // Pass arguments in registers
                registerArgs.forEachIndexed { index, arg ->
                    val asmArg = convertVal(arg)
                    instructions.add(Mov(asmArg, Register(argRegisters[index]), tackyInstr.sourceId))
                }

                instructions.add(Call(tackyInstr.funName, tackyInstr.sourceId))

                // Clean up stack
                val bytesToRemove = stackArgs.size * 8 + stackPadding
                if (bytesToRemove > 0) {
                    instructions.add(DeAllocateStack(bytesToRemove, tackyInstr.sourceId))
                }

                // Retrieve return value
                instructions.add(Mov(Register(HardwareRegister.EAX), convertVal(tackyInstr.dest), tackyInstr.sourceId))

                instructions
            }
        }

    private fun convertVal(tackyVal: TackyVal): Operand =
        when (tackyVal) {
            is TackyConstant -> Imm(tackyVal.value)
            is TackyVar -> Pseudo(tackyVal.name)
        }

    private fun convertOp(tackyOp: TackyUnaryOP): AsmUnaryOp =
        when (tackyOp) {
            TackyUnaryOP.COMPLEMENT -> AsmUnaryOp.NOT
            TackyUnaryOP.NEGATE -> AsmUnaryOp.NEG
            else -> throw IllegalArgumentException("Cannot convert $tackyOp to a simple AsmUnaryOp.")
        }
}
