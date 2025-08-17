package org.example.tacky

import assembly.Add
import assembly.AsmFunction
import assembly.AsmProgram
import assembly.AsmUnary
import assembly.AsmUnaryOp
import assembly.Cdq
import assembly.HardwareRegister
import assembly.Idiv
import assembly.Imm
import assembly.Imul
import assembly.Instruction
import assembly.Mov
import assembly.Operand
import assembly.Pseudo
import assembly.Register
import assembly.Ret
import assembly.Sub

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
            is TackyUnary -> {
                val srcOperand = convertVal(tackyInstr.src)
                val destOperand = convertVal(tackyInstr.dest)
                listOf(
                    Mov(srcOperand, destOperand),
                    AsmUnary(convertOp(tackyInstr.operator), destOperand)
                )
            }

            is TackyBinary -> {
                val srcOp1 = convertVal(tackyInstr.src1)
                val srcOp2 = convertVal(tackyInstr.src2)
                val desOp = convertVal(tackyInstr.dest)
                when (tackyInstr.operator) {
                    TackyBinaryOP.ADD ->
                        listOf(
                            Mov(srcOp1, desOp),
                            Add(srcOp2, desOp)
                        )
                    TackyBinaryOP.MULTIPLY ->
                        listOf(
                            Mov(srcOp1, desOp),
                            Imul(srcOp2, desOp)
                        )
                    TackyBinaryOP.SUBTRACT ->
                        listOf(
                            Mov(srcOp1, desOp),
                            Sub(srcOp2, desOp)
                        )
                    TackyBinaryOP.DIVIDE ->
                        listOf(
                            Mov(srcOp1, Register(HardwareRegister.EAX)),
                            Cdq,
                            Idiv(srcOp2),
                            Mov(Register(HardwareRegister.EAX), desOp)
                        )
                }
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
        }
}
