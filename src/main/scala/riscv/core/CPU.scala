// mycpu is freely redistributable under the MIT License. See the file
// "LICENSE" for information on usage and redistribution of this file.

package riscv.core

import chisel3._
import chisel3.util.Cat
import riscv.CPUBundle
import riscv.Parameters
import riscv.FDEXBundle
import riscv.EXWBBundle

class CPU extends Module {
  val io = IO(new CPUBundle)

  val regs       = Module(new RegisterFile)
  val inst_fetch = Module(new InstructionFetch)
  val id         = Module(new InstructionDecode)
  val ex         = Module(new Execute)
  val mem        = Module(new MemoryAccess)
  val wb         = Module(new WriteBack)

  val fd_ex = RegInit(0.U.asTypeOf(new FDEXBundle))
  val ex_wb = RegInit(0.U.asTypeOf(new EXWBBundle))

  // deviceSelect
  io.deviceSelect := mem.io.memory_bundle
    .address(Parameters.AddrBits - 1, Parameters.AddrBits - Parameters.SlaveDeviceCountBits)

  // IF-------------------
  inst_fetch.io.jump_address_id       := ex_wb.if_jump_address
  inst_fetch.io.jump_flag_id          := ex_wb.if_jump_flag
  inst_fetch.io.instruction_valid     := io.instruction_valid
  inst_fetch.io.instruction_read_data := io.instruction
  io.instruction_address              := inst_fetch.io.instruction_address

  // ID-------------------
  id.io.instruction := inst_fetch.io.instruction



  // -----------------(pipeline)FD_EX register---------------------
  fd_ex.instruction         := inst_fetch.io.instruction
  fd_ex.instruction_address := inst_fetch.io.instruction_address
  fd_ex.immediate           := id.io.ex_immediate
  fd_ex.ex_aluop1_source       := id.io.ex_aluop1_source
  fd_ex.ex_aluop2_source       := id.io.ex_aluop2_source
  fd_ex.reg_read_address1   := id.io.regs_reg1_read_address
  fd_ex.reg_read_address2   := id.io.regs_reg2_read_address
  fd_ex.stall               := ex_wb.if_jump_flag //first stall
  fd_ex.memory_read_enable  := id.io.memory_read_enable
  fd_ex.memory_write_enable := id.io.memory_write_enable
  fd_ex.wb_reg_write_source := id.io.wb_reg_write_source
  fd_ex.reg_write_enable    := id.io.reg_write_enable
  fd_ex.reg_write_address   := id.io.reg_write_address
  //--------------------------------------



  // Register
  regs.io.read_address1 := fd_ex.reg_read_address1
  regs.io.read_address2 := fd_ex.reg_read_address2
  regs.io.debug_read_address := io.debug_read_address
  io.debug_read_data         := regs.io.debug_read_data

  // EX
  ex.io.instruction         := fd_ex.instruction
  ex.io.instruction_address := fd_ex.instruction_address
  
  when(ex_wb.reg_write_enable && (ex_wb.reg_write_address === fd_ex.reg_read_address1)) {
    when(ex_wb.memory_read_enable) {
      ex.io.reg1_data := mem.io.wb_memory_read_data
    }.otherwise {
      ex.io.reg1_data := ex_wb.mem_alu_result
    }
  }.otherwise {
    ex.io.reg1_data := regs.io.read_data1
  }
  when(ex_wb.reg_write_enable && (ex_wb.reg_write_address === fd_ex.reg_read_address2)) {
    when(ex_wb.memory_read_enable) {
      ex.io.reg2_data := mem.io.wb_memory_read_data
    }.otherwise {
      ex.io.reg2_data := ex_wb.mem_alu_result
    }
  }.otherwise {
    ex.io.reg2_data := regs.io.read_data2
  }
  ex.io.immediate           := fd_ex.immediate
  ex.io.aluop1_source       := fd_ex.ex_aluop1_source
  ex.io.aluop2_source       := fd_ex.ex_aluop2_source



  // -------------------(pipeline)EX_WB register-------------------
  ex_wb.instruction         := fd_ex.instruction
  ex_wb.instruction_address := fd_ex.instruction_address
  ex_wb.mem_alu_result      := ex.io.mem_alu_result
  ex_wb.reg2_data           := ex.io.reg2_data
  // ex_wb.if_jump_flag        := ex.io.if_jump_flag
  ex_wb.if_jump_address     := ex.io.if_jump_address

  ex_wb.wb_reg_write_source := fd_ex.wb_reg_write_source
  ex_wb.memory_read_enable  := fd_ex.memory_read_enable
  ex_wb.reg_write_address   := fd_ex.reg_write_address

  //disable regWE&memWE
  when(fd_ex.stall || ex_wb.if_jump_flag) {
    ex_wb.if_jump_flag        := false.B
    ex_wb.reg_write_enable    := false.B
    ex_wb.memory_write_enable := false.B
  }.otherwise {
    // ex_wb.wbcontrol := fd_ex.wbcontrol
    ex_wb.memory_write_enable := fd_ex.memory_write_enable
    ex_wb.reg_write_enable    := fd_ex.reg_write_enable
    ex_wb.if_jump_flag        := ex.io.if_jump_flag
  }

  // --------------------------------------
  

  // MEM-------------------
  mem.io.alu_result          := ex_wb.mem_alu_result
  mem.io.reg2_data           := ex_wb.reg2_data
  mem.io.memory_read_enable  := ex_wb.memory_read_enable
  mem.io.memory_write_enable := ex_wb.memory_write_enable
  mem.io.funct3              := ex_wb.instruction(14, 12)
  regs.io.write_enable  := ex_wb.reg_write_enable
  regs.io.write_address := ex_wb.reg_write_address
  regs.io.write_data    := wb.io.regs_write_data

  io.memory_bundle.address := Cat(
    0.U(Parameters.SlaveDeviceCountBits.W),
    mem.io.memory_bundle.address(Parameters.AddrBits - 1 - Parameters.SlaveDeviceCountBits, 0)
  )
  io.memory_bundle.write_enable  := mem.io.memory_bundle.write_enable
  io.memory_bundle.write_data    := mem.io.memory_bundle.write_data
  io.memory_bundle.write_strobe  := mem.io.memory_bundle.write_strobe
  mem.io.memory_bundle.read_data := io.memory_bundle.read_data

  // WB--------------------
  wb.io.instruction_address := ex_wb.instruction_address
  wb.io.alu_result          := ex_wb.mem_alu_result
  wb.io.memory_read_data    := mem.io.wb_memory_read_data
  wb.io.regs_write_source   := ex_wb.wb_reg_write_source
}