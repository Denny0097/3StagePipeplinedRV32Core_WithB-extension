package riscv.core
package riscv

import chisel3._
import chisel3.util.Cat



// // WB include MEM and WB
// class WBControl extends Bundle {
//     val memory_read_enable  = Bool()
//     val memory_write_enable = Bool()
//     val wb_reg_write_source = UInt(2.W)
//     val reg_write_enable    = Bool()
//     val reg_write_address   = UInt(Parameters.PhysicalRegisterAddrWidth)
//     override def toPrintable: Printable = {
//         cf"  mem_RE: $memory_read_enable" +
//         cf"  mem_WE: $memory_write_enable" +
//         cf"  wb_reg_src: $wb_reg_write_source" +
//         cf"  reg_WE: $reg_write_enable" +
//         cf"  reg_WA: $reg_write_address\n"
//     }
// }


// class FD_EXBundle extends Bundle {
//     val instruction         = UInt(Parameters.InstructionWidth)
//     val instruction_address = UInt(Parameters.AddrWidth)
//     val immediate           = UInt(Parameters.DataWidth)
//     val ex_aluop1_source    = UInt(1.W)
//     val ex_aluop2_source    = UInt(1.W)
//     val reg_read_address1   = UInt(Parameters.PhysicalRegisterAddrWidth)
//     val reg_read_address2   = UInt(Parameters.PhysicalRegisterAddrWidth)
//     val stall               = Bool()
//     val wbcontrol           = new WBControl
//     override def toPrintable: Printable = {
//         cf"  inst: 0x$instruction%x" +
//         cf"  instAddr: $instruction_address" +
//         cf"  imm: $immediate" +
//         cf"  op1_src: $ex_aluop1_source" +
//         cf"  op2_src: $ex_aluop2_source" +
//         cf"  reg1_RA: $reg_read_address1" +
//         cf"  reg2_RA: $reg_read_address2" +
//         cf"  stall: $stall" +
//         cf"  $wbcontrol"
//     }
// }

// class EX_WBBundle extends Bundle {
//     val instruction         = UInt(Parameters.InstructionWidth)
//     val instruction_address = UInt(Parameters.AddrWidth)
//     val mem_alu_result      = UInt(Parameters.DataWidth)
//     val reg2_data           = UInt(Parameters.DataWidth)
//     val if_jump_flag        = Bool()
//     val if_jump_address     = UInt(Parameters.DataWidth)
//     val stall               = Bool()
//     val wbcontrol           = new WBControl
//     override def toPrintable: Printable = {
//         cf"  inst: 0x$instruction%x" +
//         cf"  instAddr: $instruction_address" +
//         cf"  alu_out: $mem_alu_result" +
//         cf"  reg2_data: $reg2_data" +
//         cf"  jump_flag: $if_jump_flag" +
//         cf"  jumpAddr: $if_jump_address" +
//         cf"  stall: $stall" +
//         cf"  $wbcontrol"
//     }
// }