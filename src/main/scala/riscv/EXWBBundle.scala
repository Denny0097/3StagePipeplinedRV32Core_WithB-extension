// mycpu is freely redistributable under the MIT License. See the file
// "LICENSE" for information on usage and redistribution of this file.

package riscv

import chisel3._
import peripheral.RAMBundle


class EXWBBundle extends Bundle {
  val inst_addr         = UInt(Parameters.InstructionWidth)
  val instruction       = UInt(Parameters.InstructionWidth)
  val reg2_rd           = UInt(Parameters.InstructionWidth)
  val jumpflag          = Bool()
  val alu_result        = UInt(Parameters.InstructionWidth)
  val reg_write_source  = UInt(2.W)
  val mem_write_enable  = Bool()
  val mem_read_enalbe   = Bool()
}