package riscv.core

import chisel3._
import chisel3.util.Cat
import chisel3.util.MuxLookup
import riscv.Parameters


class DataHarzardDetection  extends Module {
    val io = IO(new Bundle {
    val instruction         = Input(UInt(Parameters.InstructionWidth))
    val instruction_address = Input(UInt(Parameters.AddrWidth))
    val reg1_data           = Input(UInt(Parameters.DataWidth))
    val reg2_data           = Input(UInt(Parameters.DataWidth))
    val immediate           = Input(UInt(Parameters.DataWidth))
    val aluop1_source       = Input(UInt(1.W))
    val aluop2_source       = Input(UInt(1.W))

    val mem_alu_result  = Output(UInt(Parameters.DataWidth))
    val if_jump_flag    = Output(Bool())
    val if_jump_address = Output(UInt(Parameters.DataWidth))    
    })
  
    // Control Hazard
    when(ex_wb.jump_flag) {
        flush := true.B
    }.otherwise {
        flush := false.B
    }

}
