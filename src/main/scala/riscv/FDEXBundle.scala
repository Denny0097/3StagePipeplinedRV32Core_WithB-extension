// mycpu is freely redistributable under the MIT License. See the file
// "LICENSE" for information on usage and redistribution of this file.

package riscv

import chisel3._
import peripheral.RAMBundle

class FDEXBundle extends Bundle {
val instruction         = UInt(Parameters.InstructionWidth)
val instruction_address = UInt(Parameters.AddrWidth)
val immediate           = UInt(Parameters.DataWidth)
val ex_aluop1_source       = UInt(1.W)
val ex_aluop2_source       = UInt(1.W)
val reg_read_address1   = UInt(Parameters.PhysicalRegisterAddrWidth)
val reg_read_address2   = UInt(Parameters.PhysicalRegisterAddrWidth)
val stall               = Bool()
  
val memory_read_enable  = Bool()
val memory_write_enable = Bool()  
val wb_reg_write_source    = UInt(2.W)
val reg_write_enable    = Bool()
val reg_write_address   = UInt(Parameters.PhysicalRegisterAddrWidth)
}
