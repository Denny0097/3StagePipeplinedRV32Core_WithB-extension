# Construct a single-cycle RISC-V CPU with Chisel


## [3 stage pipeline](https://github.com/Denny0097/3StagePipeplinedRV32Core_WithB-extension/tree/3-Stage-Pipeline)

Pipeline

![3-Stage Pipeline(Reg at EX stage).drawio](https://hackmd.io/_uploads/rJ-9PKcD1e.png)
### FD stage(IF ID)
![image](https://github.com/user-attachments/assets/60df53af-ecdc-483b-8a65-bc61ea3dcff8)


### FD_EX register
```scala
class FDEXBundle extends Bundle {
val instruction_address = UInt(Parameters.AddrWidth)
val instruction         = UInt(Parameters.InstructionWidth)
val reg_read_address1   = UInt(Parameters.PhysicalRegisterAddrWidth)
val reg_read_address2   = UInt(Parameters.PhysicalRegisterAddrWidth)
val immediate           = UInt(Parameters.DataWidth)
val ex_aluop1_source       = UInt(1.W)
val ex_aluop2_source       = UInt(1.W)
val stall               = Bool()
  
val memory_read_enable  = Bool()
val memory_write_enable = Bool()  
val wb_reg_write_source    = UInt(2.W)
val reg_write_enable    = Bool()
val reg_write_address   = UInt(Parameters.PhysicalRegisterAddrWidth)
}
```
### EX stage(REG EX)
![image](https://github.com/user-attachments/assets/dfd2457e-3a91-4e11-bc1b-49b044c00606)


### EX_WB register

```scala
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
```
### WB stage(MEM WB)
![截圖 2025-01-19 晚上10.46.28](https://hackmd.io/_uploads/H1zIgc5Dyx.png)
![截圖 2025-01-19 晚上10.46.45](https://hackmd.io/_uploads/B1XDlq9Pkg.png)



## B-extension

each of these smaller extensions is grouped by common function and use case, and each has its own Zb*-extension name.
Some instructions are available in only one extension while others are available in several
#### Zba(3 ins avalible in RV32):
    sh1add, sh2add, sh3add,


    
#### Zbb(18 ins avalible in RV32):
    andn, orn, xnor, 
    clz, ctz, cpop, 
    max, maxu, min, minu,
    sext.b, sext.h, zext.h
    rol, ror, rori,
    orc.b, rev8

#### Zbc(3 ins avalible in RV32):
    clmul, clmulh, clmulr, 
    
#### Zbs(8 ins avalible in RV32):
    bclr, bclri, bext, bexti, binv, binvi, bset, bseti

    

### Decoder

### 1. IB

#### opcode : `0010011`(I-type)
Decode order : Opcode -> Funct3 -> Funct7 -> Shamt


| funct7     | shamt    | rs1       | funct3 | rd        | opcode    | Instruction(s) |
|------------|----------|----------|--------|-----------|-----------|----------------|
IB1
| 0010100    |5 bits   | 5 bits    | 001    | 5 bits    | 0010011   | bseti          |
| 0100100    | 5 bits   | 5 bits    | 001    | 5 bits    | 0010011   | bclri          |
| 0110000    | 00000    | 5 bits    | 001    | 5 bits    | 0010011   | clz            |
| 0110000    | 00001    | 5 bits    | 001    | 5 bits    | 0010011   | ctz            |
| 0110000    | 00010    | 5 bits    | 001    | 5 bits    | 0010011   | cpop           |
| 0110000    | 00100    | 5 bits    | 001    | 5 bits    | 0010011   | sext.b         |
| 0110000    | 00101    | 5 bits    | 001    | 5 bits    | 0010011   | sext.h         |
| 0110100    | 5 bits   | 5 bits    | 001    | 5 bits    | 0010011   | binvi          |
IB2
| 0010100    | 00111    | 5 bits    | 101    | 5 bits    | 0010011   | orc.b          |
| 0100100    | 5 bits   | 5 bits    | 101    | 5 bits    | 0010011   | bexti          |
| 0110100    | 11000    | 5 bits    | 101    | 5 bits    | 0010011   | rev8           |




### 2.RB

#### opcode : `0110011`(RMType)
Decode order : Opcode -> Funct7 -> Funct3 -> Shamt


| funct7   | rs2       | rs1       | funct3 | rd        | opcode    | instruction |
|----------|-----------|-----------|--------|-----------|-----------|-------------|
**zext.h**
| 0000100  | `00000`   | 5 bits    | 100    | 5 bits    | 0110011   | zext.h      |
**best**
| 0010100  | 5 bits    | 5 bits    | 001    | 5 bits    | 0110011   | bset    |
**binv**
| 0110100  | 5 bits    | 5 bits    | 001    | 5 bits    | 0110011   | binv        |
**RB1**
| 0000101  | 5 bits    | 5 bits    | 001    | 5 bits    | 0110011   | clmul       |
| 0000101  | 5 bits    | 5 bits    | 010    | 5 bits    | 0110011   | clmulr      |
| 0000101  | 5 bits    | 5 bits    | 011    | 5 bits    | 0110011   | clmulh      |
| 0000101  | 5 bits    | 5 bits    | 100    | 5 bits    | 0110011   | min         |
| 0000101  | 5 bits    | 5 bits    | 101    | 5 bits    | 0110011   | minu        |
| 0000101  | 5 bits    | 5 bits    | 110    | 5 bits    | 0110011   | max         |
| 0000101  | 5 bits    | 5 bits    | 111    | 5 bits    | 0110011   | maxu        |
**RB2**
| 0010000  | 5 bits    | 5 bits    | 010    | 5 bits    | 0110011   | sh1add      |
| 0010000  | 5 bits    | 5 bits    | 100    | 5 bits    | 0110011   | sh2add      |
| 0010000  | 5 bits    | 5 bits    | 110    | 5 bits    | 0110011   | sh3add      |
**RB3**
| 0100000  | 5 bits    | 5 bits    | 100    | 5 bits    | 0110011   | xnor        |
| 0100000  | 5 bits    | 5 bits    | 110    | 5 bits    | 0110011   | orn         |
| 0100000  | 5 bits    | 5 bits    | 111    | 5 bits    | 0110011   | andn        |
**RB4**
| 0100100  | 5 bits    | 5 bits    | 001    | 5 bits    | 0110011   | bclr        |
| 0100100  | 5 bits    | 5 bits    | 101    | 5 bits    | 0110011   | bext        |
**RB5**
| 0110000  | 5 bits    | 5 bits    | 001    | 5 bits    | 0110011   | rol         |
| 0110000  | 5 bits    | 5 bits    | 101    | 5 bits    | 0110011   | ror         |





## Test
Test the execution of pepeline and whether adding extension causes errors.
```
sbt test
```
![image](https://hackmd.io/_uploads/H1VPHRgdJg.png)


> [!WARNING]
> Please be aware that the Scala code in this repository is not entirely complete, as the instructor has omitted certain sections for students to work on independently.

## Development Objectives

Our goal is to create a RISC-V CPU that prioritizes simplicity while assuming a foundational understanding of digital circuits and the C programming language among its readers. The CPU should strike a balance between simplicity and sophistication, and we intend to maximize its functionality. This project encompasses the following key aspects, which will be prominently featured in the technical report:
* Implementation in Chisel.
* RV32I instruction set support.
* Execution of programs compiled from the C programming language.
