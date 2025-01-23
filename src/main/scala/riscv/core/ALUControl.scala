// mycpu is freely redistributable under the MIT License. See the file
// "LICENSE" for information on usage and redistribution of this file.

package riscv.core

import chisel3._
import chisel3.util._

class ALUControl extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))
    val funct3 = Input(UInt(3.W))
    val funct7 = Input(UInt(7.W))
    val shamt  = Input(UInt(5.W))

    val alu_funct = Output(ALUFunctions())
  })

  io.alu_funct := ALUFunctions.zero

  switch(io.opcode) {
    is(InstructionTypes.IBex) {
      io.alu_funct := MuxLookup(
        io.funct3,
        ALUFunctions.zero,
        IndexedSeq(
          InstructionsTypeI.addi  -> ALUFunctions.add,
          InstructionsTypeI.slli  -> MuxLookup(
            io.funct7,
            ALUFunctions.sll,
              IndexedSeq(
              IB1.bseti -> ZbsFunctions.bseti,
              IB1.binvi -> ZbsFunctions.binvi,
              IB1.bclri -> ZbsFunctions.bclri,
              IB1.Zbb1 -> MuxLookup(
                shamt,
                ZbbFunctions.clz,
                IndexedSeq(
                  Zbb1.ctz -> ZbbFunctions.ctz,
                  Zbb1.cpop ->ZbbFunctions.cpop,
                  Zbb1.sextb->ZbbFunctions.sextb,
                  Zbb1.sexth->ZbbFunctions.sexth
                )
              )
            )
          ),
          InstructionsTypeI.slti  -> ALUFunctions.slt,
          InstructionsTypeI.sltiu -> ALUFunctions.sltu,
          InstructionsTypeI.xori  -> ALUFunctions.xor,
          InstructionsTypeI.ori   -> ALUFunctions.or,
          InstructionsTypeI.andi  -> ALUFunctions.and,
          InstructionsTypeI.sri   -> MuxLookup(
            io.funct7, 
            ALUFunctions.srl, 
            IndexedSeq(
              IB2.srai -> ALUFunctions.sra,
              IB2.rori -> ZbbFunctions.rori,
              IB2.rev8 -> ZbbFunctions.rev8,
              IB2.orcb -> ZbbFunctions.orcb,
              IB2.bexti-> ZbsFunctions.bexti
            )
          )
        ),
      )
    }

    is(InstructionTypes.RM) {
      io.alu_funct := MuxLookup(
        io.funct7,
        DontCare,
        InstructionsTypeRorRB.InstructionsTypeR -> MuxLookup(
          io.funct3,
          ALUFunctions.zero,
          IndexedSeq(
            InstructionsTypeR.add_sub -> Mux(io.funct7(5), ALUFunctions.sub, ALUFunctions.add),
            InstructionsTypeR.sll     -> ALUFunctions.sll,
            InstructionsTypeR.slt     -> ALUFunctions.slt,
            InstructionsTypeR.sltu    -> ALUFunctions.sltu,
            InstructionsTypeR.xor     -> ALUFunctions.xor,
            InstructionsTypeR.or      -> ALUFunctions.or,
            InstructionsTypeR.and     -> ALUFunctions.and,
            InstructionsTypeR.sr      -> ALUFunctions.srl
          ),
        ),
        InstructionsTypeRorRB.zexth   -> ZbbFunctions.zexth,
        InstructionsTypeRorRB.bset    -> ZbsFunctions.bset,
        InstructionsTypeRorRB.binv    -> ZbsFunctions.binv,
        InstructionsTypeRorRB.RB1     -> MuxLookup(
          io.funct3,
          ALUFunctions.zero,
          IndexedSeq(
            RB1.clmul  ->  ZbcFunctions.clmul,
            RB1.clmulr ->  ZbcFunctions.clmulr,
            RB1.clmulh ->  ZbcFunctions.clmulh,
            RB1.min    ->  ZbbFunctions.min, 
            RB1.minu   ->  ZbbFunctions.minu, 
            RB1.max    ->  ZbbFunctions.max, 
            RB1.maxu   ->  ZbbFunctions.maxu
          )
        ),
        InstructionsTypeRorRB.RB2     -> MuxLookup (
          io.funct3,
          ALUFunctions.zero,
          IndexedSeq(
            RB2.sh1add ->  ZbaFunctions.sh1add,
            RB2.sh2add ->  ZbaFunctions.sh2add,
            RB2.sh3add ->  ZbaFunctions.sh3add
          )
        ),
        InstructionsTypeRorRB.sraorRB3 -> MuxLookup(
          io.funct3,
          ALUFunctions.zero,
          IndexedSeq(
            RB3.sra    ->  ALUFunctions.sra,
            RB3.xnor   ->  ZbbFunctions.xnor,
            RB3.orn    ->  ZbbFunctions.orn,
            RB3.andn   ->  ZbbFunctions.andn   
          )
        ),
        InstructionsTypeRorRB.RB4     -> Mux(io.funct3(2), ZbsFunctions.bclr, ZbsFunctions.bext),
        InstructionsTypeRorRB.RB5     -> Mux(io.funct3(2), ZbsFunctions.rol, ALUFunctions.ror),
      )
    }
    
    is(InstructionTypes.B) {
      io.alu_funct := ALUFunctions.add
    }
    is(InstructionTypes.L) {
      io.alu_funct := ALUFunctions.add
    }
    is(InstructionTypes.S) {
      io.alu_funct := ALUFunctions.add
    }
    is(Instructions.jal) {
      io.alu_funct := ALUFunctions.add
    }
    is(Instructions.jalr) {
      io.alu_funct := ALUFunctions.add
    }
    is(Instructions.lui) {
      io.alu_funct := ALUFunctions.add
    }
    is(Instructions.auipc) {
      io.alu_funct := ALUFunctions.add
    }
  }
}
