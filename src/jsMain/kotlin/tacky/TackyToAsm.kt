package tacky

import assembly.AsmBinary
import assembly.AsmBinaryOp
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Cdq
import assembly.Cmp
import assembly.ConditionCode
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
import assembly.Register
import assembly.Ret
import assembly.SetCC

class TackyToAsm {
    fun convert(tackyProgram: TackyProgram): AsmProgram {
        val tackyFunction = tackyProgram.function
        val asmInstructions = tackyFunction.body.flatMap { convertInstruction(it) }

        val asmFunction = AsmFunction(tackyFunction.name, asmInstructions)
        return AsmProgram(asmFunction)
    }

    private fun convertInstruction(tackyInstr: TackyInstruction): List<Instruction> =
        when (tackyInstr) {
            is TackyRet -> {
                listOf(
                    Mov(convertVal(tackyInstr.value), Register(HardwareRegister.EAX)),
                    Ret
                )
            }
            is TackyUnary ->
                when (tackyInstr.operator) {
                    TackyUnaryOP.NOT -> {
                        val src = convertVal(tackyInstr.src)
                        val dest = convertVal(tackyInstr.dest)
                        listOf(
                            Cmp(Imm(0), src),
                            Mov(Imm(0), dest), // Zero out destination
                            SetCC(ConditionCode.E, dest) // Set if equal to zero
                        )
                    }
                    else -> {
                        val destOperand = convertVal(tackyInstr.dest)
                        listOf(
                            Mov(convertVal(tackyInstr.src), destOperand),
                            AsmUnary(convertOp(tackyInstr.operator), destOperand)
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
                            Mov(src1, dest),
                            AsmBinary(AsmBinaryOp.ADD, src2, dest)
                        )
                    TackyBinaryOP.MULTIPLY ->
                        listOf(
                            Mov(src1, dest),
                            AsmBinary(AsmBinaryOp.MUL, src2, dest)
                        )
                    TackyBinaryOP.SUBTRACT ->
                        listOf(
                            Mov(src1, dest),
                            AsmBinary(AsmBinaryOp.SUB, src2, dest)
                        )
                    TackyBinaryOP.DIVIDE ->
                        listOf(
                            Mov(src1, Register(HardwareRegister.EAX)), // Dividend in EAX
                            Cdq,
                            Idiv(src2), // Divisor
                            Mov(Register(HardwareRegister.EAX), dest) // Quotient result is in EAX
                        )
                    TackyBinaryOP.REMAINDER ->
                        listOf(
                            Mov(src1, Register(HardwareRegister.EAX)),
                            Cdq,
                            Idiv(src2),
                            Mov(Register(HardwareRegister.EDX), dest) // Remainder result is in EDX
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
                            Mov(src1, Register(HardwareRegister.EAX)),
                            Cmp(src2, Register(HardwareRegister.EAX)),
                            Mov(Imm(0), dest), // Zero out destination
                            SetCC(condition, dest) // conditionally set the low byte to 0/1
                        )
                    }
                }
            }

            is JumpIfNotZero ->
                listOf(
                    Cmp(Imm(0), convertVal(tackyInstr.condition)),
                    JmpCC(ConditionCode.NE, Label(tackyInstr.target.name))
                )

            is JumpIfZero ->
                listOf(
                    Cmp(Imm(0), convertVal(tackyInstr.condition)),
                    JmpCC(ConditionCode.E, Label(tackyInstr.target.name))
                )

            is TackyCopy -> listOf(Mov(convertVal(tackyInstr.src), convertVal(tackyInstr.dest)))

            is TackyJump -> listOf(Jmp(Label(tackyInstr.target.name)))

            is TackyLabel -> listOf(Label(tackyInstr.name))
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
