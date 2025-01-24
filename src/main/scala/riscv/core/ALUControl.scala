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
              ALUFunctions.zero,
              IndexedSeq(
              IB1.slli  -> ALUFunctions.sll,
              IB1.bseti -> ALUFunctions.bseti,
              IB1.binvi -> ALUFunctions.binvi,
              IB1.bclri -> ALUFunctions.bclri,
              IB1.Zbb1 -> MuxLookup(
                io.shamt,
                ALUFunctions.zero,
                IndexedSeq(
                  Zbb1.clz -> ALUFunctions.clz,
                  Zbb1.ctz -> ALUFunctions.ctz,
                  Zbb1.cpop ->ALUFunctions.cpop,
                  Zbb1.sextb->ALUFunctions.sextb,
                  Zbb1.sexth->ALUFunctions.sexth
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
            ALUFunctions.zero, 
            IndexedSeq(
              IB2.srli -> ALUFunctions.srl,
              IB2.srai -> ALUFunctions.sra,
              IB2.rori -> ALUFunctions.rori,
              IB2.rev8 -> ALUFunctions.rev8,
              IB2.orcb -> ALUFunctions.orcb,
              IB2.bexti-> ALUFunctions.bexti
            )
          )
        ),
      )
    }

    is(InstructionTypes.RMBex) {
      io.alu_funct := MuxLookup(
        io.funct7,
        ALUFunctions.zero,
        IndexedSeq(
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
          InstructionsTypeRorRB.zexth   -> ALUFunctions.zexth,
          InstructionsTypeRorRB.bset    -> ALUFunctions.bset,
          InstructionsTypeRorRB.binv    -> ALUFunctions.binv,
          InstructionsTypeRorRB.RB1     -> MuxLookup(
            io.funct3,
            ALUFunctions.zero,
            IndexedSeq(
              RB1.clmul  ->  ALUFunctions.clmul,
              RB1.clmulr ->  ALUFunctions.clmulr,
              RB1.clmulh ->  ALUFunctions.clmulh,
              RB1.min    ->  ALUFunctions.min, 
              RB1.minu   ->  ALUFunctions.minu, 
              RB1.max    ->  ALUFunctions.max, 
              RB1.maxu   ->  ALUFunctions.maxu
            )
          ),
          InstructionsTypeRorRB.RB2     -> MuxLookup (
            io.funct3,
            ALUFunctions.zero,
            IndexedSeq(
              RB2.sh1add ->  ALUFunctions.sh1add,
              RB2.sh2add ->  ALUFunctions.sh2add,
              RB2.sh3add ->  ALUFunctions.sh3add
            )
          ),
          InstructionsTypeRorRB.sraorRB3 -> MuxLookup(
            io.funct3,
            ALUFunctions.zero,
            IndexedSeq(
              RB3.sra    ->  ALUFunctions.sra,
              RB3.xnor   ->  ALUFunctions.xnor,
              RB3.orn    ->  ALUFunctions.orn,
              RB3.andn   ->  ALUFunctions.andn   
            )
          ),
          InstructionsTypeRorRB.RB4     -> Mux(io.funct3(2), ALUFunctions.bclr, ALUFunctions.bext),
          InstructionsTypeRorRB.RB5     -> Mux(io.funct3(2), ALUFunctions.rol, ALUFunctions.ror),
        )
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
