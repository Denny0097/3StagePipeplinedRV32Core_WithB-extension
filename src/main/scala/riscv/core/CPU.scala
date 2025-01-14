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
  val fd_ex = RegInit(0.U.asTypeOf(new FDEXBundle))
  val ex_wb = RegInit(0.U.asTypeOf(new EXWBBundle))


  val regs       = Module(new RegisterFile)
  val inst_fetch = Module(new InstructionFetch)
  val id         = Module(new InstructionDecode)
  val ex         = Module(new Execute)
  val mem        = Module(new MemoryAccess)
  val wb         = Module(new WriteBack)



  io.deviceSelect := mem.io.memory_bundle
    .address(Parameters.AddrBits - 1, Parameters.AddrBits - Parameters.SlaveDeviceCountBits)

  // IF
  inst_fetch.io.jump_address_id       := ex.io.if_jump_address
  inst_fetch.io.jump_flag_id          := ex.io.if_jump_flag
  inst_fetch.io.instruction_valid     := io.instruction_valid
  inst_fetch.io.instruction_read_data := io.instruction
  io.instruction_address              := inst_fetch.io.instruction_address


  // ID
  regs.io.write_enable  := id.io.reg_write_enable
  regs.io.write_address := id.io.reg_write_address
  regs.io.write_data    := wb.io.regs_write_data
  regs.io.read_address1 := id.io.regs_reg1_read_address
  regs.io.read_address2 := id.io.regs_reg2_read_address

  regs.io.debug_read_address := io.debug_read_address
  io.debug_read_data         := regs.io.debug_read_data

  id.io.instruction := inst_fetch.io.instruction




  // -----------------(pipeline)FDEX register---------------------
  fd_ex.inst_addr := regs.io.write_address
  fd_ex.instruction := id.io.instruction
  fd_ex.reg1_data := regs.io.read_address1
  fd_ex.reg2_data := regs.io.read_address2
  fd_ex.imm :=  id.io.ex_immediate
  fd_ex.aluop1_source := id.io.ex_aluop1_source
  fd_ex.aluop2_source := id.io.ex_aluop2_source
  fd_ex.reg_write_source := id.io.wb_reg_write_source
  fd_ex.mem_write_enable := id.io.memory_write_enable
  fd_ex.mem_read_enalbe := id.io.memory_read_enable

  
  // EX
  ex.io.reg1_data := fd_ex.reg1_data
  ex.io.reg2_data := fd_ex.reg2_data

  ex.io.instruction_address := fd_ex.inst_addr
  ex.io.instruction := fd_ex.instruction

  ex.io.aluop1_source := fd_ex.aluop1_source
  ex.io.aluop2_source := fd_ex.aluop2_source
  ex.io.immediate := fd_ex.imm


  // -------------------(pipeline)FDEX register-------------------
  ex_wb.inst_addr := ex.io.instruction_address
  ex_wb.instruction := ex.io.instruction
  ex_wb.reg2_rd := ex.io.reg2_data
  ex_wb.jumpflag := ex.io.if_jump_flag
  ex_wb.alu_result := ex.io.mem_alu_result
  ex_wb.reg_write_source := fd_ex.reg_write_source
  ex_wb.mem_write_enable := fd_ex.mem_write_enable
  ex_wb.mem_read_enalbe := fd_ex.mem_read_enalbe


  // MEM
  
  mem.io.funct3              := ex_wb.instruction(14, 12)
  mem.io.alu_result          := ex_wb.alu_result
  mem.io.reg2_data           := ex_wb.reg2_rd
  
  mem.io.memory_write_enable := ex_wb.mem_write_enable
  mem.io.memory_read_enable  := ex_wb.mem_read_enalbe

  io.memory_bundle.address := Cat(
    0.U(Parameters.SlaveDeviceCountBits.W),
    mem.io.memory_bundle.address(Parameters.AddrBits - 1 - Parameters.SlaveDeviceCountBits, 0)
  )
  io.memory_bundle.write_enable  := mem.io.memory_bundle.write_enable
  io.memory_bundle.write_data    := mem.io.memory_bundle.write_data
  io.memory_bundle.write_strobe  := mem.io.memory_bundle.write_strobe
  mem.io.memory_bundle.read_data := io.memory_bundle.read_data


  // WB
  wb.io.instruction_address := ex_wb.inst_addr
  wb.io.alu_result          := ex_wb.alu_result
  wb.io.memory_read_data    := mem.io.wb_memory_read_data
  wb.io.regs_write_source   := ex_wb.reg_write_source
}