// mycpu is freely redistributable under the MIT License. See the file
// "LICENSE" for information on usage and redistribution of this file.

package riscv

import chisel3._
import peripheral.RAMBundle

class FDEXBundle extends Bundle {
  val inst_addr         = UInt(Parameters.InstructionWidth)
  val instruction       = UInt(Parameters.InstructionWidth)
  val reg1_data         = UInt(Parameters.InstructionWidth)
  val reg2_data         = UInt(Parameters.InstructionWidth)
  val imm               = UInt(Parameters.InstructionWidth)
  val aluop1_source     = UInt(1.W)
  val aluop2_source     = UInt(1.W)
  val reg_write_source  = UInt(2.W)
  val mem_write_enable  = Bool()
  val mem_read_enable   = Bool()
  val reg_write_enable  = Bool()
  val reg_write_address = UInt(Parameters.InstructionWidth)
  val reg_read_address1 = UInt(Parameters.InstructionWidth)
  val reg_read_address2 = UInt(Parameters.InstructionWidth)
}
