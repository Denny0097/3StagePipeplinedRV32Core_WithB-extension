// mycpu is freely redistributable under the MIT License. See the file
// "LICENSE" for information on usage and redistribution of this file.

package riscv

import chisel3._
import peripheral.RAMBundle

class EXWBBundle extends Bundle {
  val instruction         = UInt(Parameters.InstructionWidth)
  val instruction_address = UInt(Parameters.AddrWidth)
  val mem_alu_result      = UInt(Parameters.DataWidth)
  val reg2_data           = UInt(Parameters.DataWidth)
  val if_jump_flag        = Bool()
  val if_jump_address     = UInt(Parameters.DataWidth)
  val stall               = Bool()


  val memory_read_enable  = Bool()
  val memory_write_enable = Bool()  
  val wb_reg_write_source    = UInt(2.W)
  val reg_write_enable    = Bool()
  val reg_write_address   = UInt(Parameters.PhysicalRegisterAddrWidth)
}