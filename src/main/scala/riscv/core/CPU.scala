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
  
  // Control signal
  val flush = RegInit(false.B) // 用於 Control Hazard
  val stall = RegInit(false.B) // 用於 lw data Hazard

  // deviceSelect
  io.deviceSelect := mem.io.memory_bundle
    .address(Parameters.AddrBits - 1, Parameters.AddrBits - Parameters.SlaveDeviceCountBits)

  // IF-------------------
  inst_fetch.io.jump_address_id       := ex_wb.if_jump_address
  inst_fetch.io.jump_flag_id          := ex_wb.jump_flag
  inst_fetch.io.instruction_valid     := io.instruction_valid
  inst_fetch.io.instruction_read_data := io.instruction
  io.instruction_address              := inst_fetch.io.instruction_address

  // Control Hazard
  when(ex_wb.jump_flag) {
    flush := true.B
  }.otherwise {
    flush := false.B
  }

  // ID-------------------
  id.io.instruction := inst_fetch.io.instruction

  regs.io.write_enable  := id.io.reg_write_enable
  regs.io.read_address1 := id.io.regs_reg1_read_address
  regs.io.read_address2 := id.io.regs_reg2_read_address
  regs.io.debug_read_address := io.debug_read_address

  io.debug_read_data         := regs.io.debug_read_data


  // -----------------(pipeline)FD_EX register---------------------
  // lw data harzard detection
  when(fd_ex.reg_write_enable && fd_ex.memory_read_enable && 
     ((fd_ex.reg_write_address === regs.io.read_address1) || 
      (fd_ex.reg_write_address === regs.io.read_address2))) {
    stall := true.B  
  }
  .otherwise{
    stall := false.B
  }


  when(flush) { //flush fd_ex reg if control harzard
    fd_ex := RegInit(0.U.asTypeOf(new FDEXBundle))
    fd_ex.instruction := 0x00000013L.U(Parameters.DataWidth)
  }
  .elsewhen(stall) {
    // 因為stall之指令在FD，只要直接flush掉fd_ex，等下一回合再次讀取即可取得正確的Rd值
    fd_ex := RegInit(0.U.asTypeOf(new FDEXBundle))
    fd_ex.instruction := 0x00000013L.U(Parameters.DataWidth)
    inst_fetch.io.instruction_valid :=  false.B  // PC 保持不變
  }  
  .otherwise{
    fd_ex.instruction_address := inst_fetch.io.instruction_address
    fd_ex.instruction := id.io.instruction
    fd_ex.reg1_data := regs.io.read_data1
    fd_ex.reg2_data := regs.io.read_data2
    fd_ex.immediate := id.io.ex_immediate
    fd_ex.aluop1_source := id.io.ex_aluop1_source
    fd_ex.aluop2_source := id.io.ex_aluop2_source
    fd_ex.reg_write_source := id.io.wb_reg_write_source
    fd_ex.memory_write_enable := id.io.memory_write_enable
    fd_ex.memory_read_enable := id.io.memory_read_enable
    fd_ex.reg_write_enable := regs.io.write_enable
    fd_ex.reg_write_address := regs.io.write_address
    fd_ex.reg_read_address1 := regs.io.read_address1
    fd_ex.reg_read_address2 := regs.io.read_address2
  }
  //--------------------------------------




  
  // EX-------------------
  ex.io.instruction_address := fd_ex.instruction_address
  ex.io.instruction := fd_ex.instruction


  // forwarding start
  when(ex_wb.reg_write_enable && (ex_wb.reg_write_address === fd_ex.reg_read_address1)) {
    // when(ex_wb.memory_read_enable) { // cuz data harzard deal at FD/EX, so don't need considering there.
    //   ex.io.reg1_data := mem.io.wb_memory_read_data
    // }.otherwise {
      ex.io.reg1_data := ex_wb.alu_result 
    // }
  }.otherwise {
    ex.io.reg1_data := fd_ex.reg1_data 
  }
    
  when(ex_wb.reg_write_enable && (ex_wb.reg_write_address === fd_ex.reg_read_address2)) {
    // when(ex_wb.memory_read_enable) {
    //   ex.io.reg2_data := mem.io.wb_memory_read_data
    // }.otherwise {
      ex.io.reg2_data := ex_wb.alu_result
    // }
  }.otherwise {
    ex.io.reg2_data := fd_ex.reg2_data
  }
  // forwarding end
  ex.io.immediate := fd_ex.immediate
  ex.io.aluop1_source := fd_ex.aluop1_source
  ex.io.aluop2_source := fd_ex.aluop2_source



  // -------------------(pipeline)EX_WB register-------------------
  ex_wb.instruction_address := fd_ex.instruction_address
  ex_wb.instruction := fd_ex.instruction
  ex_wb.read_data2 := fd_ex.reg2_data
  ex_wb.jump_flag := ex.io.if_jump_flag
  ex_wb.if_jump_address := ex.io.mem_alu_result
  ex_wb.alu_result := ex.io.mem_alu_result
  ex_wb.reg_write_source := fd_ex.reg_write_source
  ex_wb.memory_write_enable := fd_ex.memory_write_enable
  ex_wb.memory_read_enable := fd_ex.memory_read_enable
  ex_wb.reg_write_enable := fd_ex.reg_write_enable
  ex_wb.reg_write_address := fd_ex.reg_write_address


  when(flush) { //flush ex_wb reg if control harzard
    ex_wb := 0.U.asTypeOf(new EXWBBundle)
    ex_wb.instruction_address := 0x00000013L.U(Parameters.DataWidth)
  }
  // --------------------------------------
  

  // MEM-------------------
  mem.io.funct3              := ex_wb.instruction(14, 12)
  mem.io.alu_result          := ex_wb.alu_result
  mem.io.reg2_data           := ex_wb.read_data2
  mem.io.memory_write_enable := ex_wb.memory_write_enable
  mem.io.memory_read_enable  := ex_wb.memory_read_enable

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


  // WB-------------------
  wb.io.instruction_address := ex_wb.instruction_address
  wb.io.alu_result          := ex_wb.alu_result
  wb.io.memory_read_data    := mem.io.wb_memory_read_data
  wb.io.regs_write_source   := ex_wb.reg_write_source
}