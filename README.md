# Construct a single-cycle RISC-V CPU with Chisel

> [!WARNING]
> Please be aware that the Scala code in this repository is not entirely complete, as the instructor has omitted certain sections for students to work on independently.

## Development Objectives

Our goal is to create a RISC-V CPU that prioritizes simplicity while assuming a foundational understanding of digital circuits and the C programming language among its readers. The CPU should strike a balance between simplicity and sophistication, and we intend to maximize its functionality. This project encompasses the following key aspects, which will be prominently featured in the technical report:
* Implementation in Chisel.
* RV32I instruction set support.
* Execution of programs compiled from the C programming language.

# 3-stage pipeplined RV32 core with B extension
> 姜冠宇

RISC-V [B extension](https://github.com/riscv/riscv-b). 
[code](https://github.com/Denny0097/3StagePipeplinedRV32Core_WithB-extension)


## [3 stage pipeline](https://github.com/Denny0097/3StagePipeplinedRV32Core_WithB-extension/tree/3-Stage-Pipeline)

Pipeline
![3-Stage Pipeline(Reg at EX stage).drawio](https://hackmd.io/_uploads/rJ-9PKcD1e.png)
### FD stage(IF ID)
![截圖 2025-01-19 晚上10.40.47](https://hackmd.io/_uploads/ryRe195vye.png =500x)

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
![截圖 2025-01-19 晚上10.44.43](https://hackmd.io/_uploads/BJ61gcqDJe.png =600x)

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





### Decoder output
#### Zba
|Output    |sh1add    |sh2add    |sh3add|
|----------|-----------|-----------|----------|
regs_reg1_read_address:|rs1 |rs1 |rs1 |
ex_aluop1_source:|0 |0 |0 |
ex_aluop2_source:|0 |0 |0 |
memory_read_enable:|0 |0 |0 |
memory_write_enable:|0 |0 |0 |
wb_reg_write_source:|0 |0 |0 |
reg_write_enable:|1 |1 |1 |

#### Zbb

#### Zbc

#### Zbs





### Operation(EX)

#### Zba
:::spoiler <font color=gray>**Content**</font><br>
**sh1add rd, rs1, rs2:**     *X(rd) = X(rs2) + (X(rs1) << 1);*
**sh2add rd, rs1, rs2**      *X(rd) = X(rs2) + (X(rs1) << 2);*
**sh3add rd, rs1, rs2**      *X(rd) = X(rs2) + (X(rs1) << 3);*
:::

#### Zbb
:::spoiler <font color=gray>**Content**</font><br>
**andn rd, rs1, rs2:**      *X(rd) = X(rs1) & ~X(rs2);*
**orn rd, rs1, rs2:**       *X(rd) = X(rs1) | ~X(rs2);*
**xnor rd, rs1, rs2:**      *X(rd) = ~(X(rs1) ^ X(rs2));*
**clz rd, rs:**             
```
val HighestSetBit : forall ('N : Int), 'N >= 0. bits('N) -> int

function HighestSetBit x = {
  foreach (i from (xlen - 1) to 0 by 1 in dec)
    if [x[i]] == 0b1 then return(i) else ();
  return -1;
}

let rs = X(rs);
X[rd] = (xlen - 1) - HighestSetBit(rs);
```
**ctz rd, rs**
```
val LowestSetBit : forall ('N : Int), 'N >= 0. bits('N) -> int

function LowestSetBit x = {
  foreach (i from 0 to (xlen - 1) by 1 in dec)
    if [x[i]] == 0b1 then return(i) else ();
  return xlen;
}

let rs = X(rs);
X[rd] = LowestSetBit(rs);
```
**cpop rd, rs**
```
let bitcount = 0;
let rs = X(rs);

foreach (i from 0 to (xlen - 1) in inc)
    if rs[i] == 0b1 then bitcount = bitcount + 1 else ();

X[rd] = bitcount
```
**max rd, rs1, rs2**
```
let rs1_val = X(rs1);
let rs2_val = X(rs2);

let result = if   rs1_val <_s rs2_val
    	     then rs2_val
	     else rs1_val;

X(rd) = result;
```
**maxu rd, rs1, rs2**
```
let rs1_val = X(rs1);
let rs2_val = X(rs2);

let result = if   rs1_val <_u rs2_val
    	     then rs2_val
	     else rs1_val;

X(rd) = result;
```
**min rd, rs1, rs2**
```
let rs1_val = X(rs1);
let rs2_val = X(rs2);

let result = if   rs1_val <_s rs2_val
    	     then rs1_val
	     else rs2_val;

X(rd) = result;
```
**minu rd, rs1, rs2**
```
let rs1_val = X(rs1);
let rs2_val = X(rs2);

let result = if   rs1_val <_u rs2_val
    	     then rs1_val
	     else rs2_val;

X(rd) = result;
```
**sext.b rd, rs:** *X(rd) = EXTS(X(rs)[7..0]);*
**sext.h rd, rs:** *X(rd) = EXTS(X(rs)[15..0]);*
**zext.h rd, rs:** *X(rd) = EXTZ(X(rs)[15..0]);*
**rol rd, rs1, rs2**
```
let shamt = if   xlen == 32
    	    then X(rs2)[4..0]
	    else X(rs2)[5..0];
let result = (X(rs1) << shamt) | (X(rs2) >> (xlen - shamt));

X(rd) = result;
```
**ror rd, rs1, rs2**
```
let shamt = if   xlen == 32
    	    then X(rs2)[4..0]
	    else X(rs2)[5..0];
let result = (X(rs1) >> shamt) | (X(rs2) << (xlen - shamt));

X(rd) = result;
```

**rori rd, rs1, shamt**
```
let shamt = if   xlen == 32
    	    then shamt[4..0]
	    else shamt[5..0];
let result = (X(rs1) >> shamt) | (X(rs2) << (xlen - shamt));

X(rd) = result;
```
**orc.b rd, rs**
```
let input = X(rs);
let output : xlenbits = 0;
let j = xlen;

foreach (i from 0 to xlen by 8) {
   output[(i + 7)..i] = if   input[(i - 7)..i] == 0
                        then 0b00000000
                        else 0b11111111;
}

X[rd] = output;
```
**rev8 rd, rs:**
```
let input = X(rs);
let output : xlenbits = 0;
let j = xlen;

foreach (i from 0 to xlen by 8) {
   output[i..(i + 7)] = input[(j - 7)..j];
   j = j - 8;
}

X[rd] = output
```
:::

#### Zbc
:::spoiler <font color=gray>**Content**</font><br>
**clmul rd, rs1, rs2:**  clmul produces the lower half of the 2·XLEN carry-less product.
```
let rs1_val = X(rs1);
let rs2_val = X(rs2);
let output : xlenbits = 0;

foreach (i from 0 to xlen by 1) {
   output = if   ((rs2_val >> i) & 1)
            then output ^ (rs1_val << i);
	    else output;
}

X[rd] = output
```
**clmulh rd, rs1, rs2:** clmulh produces the upper half of the 2·XLEN carry-less product.
```
let rs1_val = X(rs1);
let rs2_val = X(rs2);
let output : xlenbits = 0;

foreach (i from 1 to xlen by 1) {
   output = if   ((rs2_val >> i) & 1)
            then output ^ (rs1_val >> (xlen - i));
	    else output;
}

X[rd] = output
```
**clmulr rd, rs1, rs2:** produces bits 2·XLEN−2:XLEN-1 of the 2·XLEN carry-less product.

Operation

```
let rs1_val = X(rs1);
let rs2_val = X(rs2);
let output : xlenbits = 0;

foreach (i from 0 to (xlen - 1) by 1) {
   output = if   ((rs2_val >> i) & 1)
            then output ^ (rs1_val >> (xlen - i - 1));
	    else output;
}

X[rd] = output
```

:::
#### Zbs
:::spoiler <font color=gray>**Content**</font><br>
**bclr rd, rs1, rs2:** This instruction returns rs1 with a single bit cleared at the index specified in rs2. The index is read from the lower log2(XLEN) bits of rs2.
```
let index = X(rs2) & (XLEN - 1);
X(rd) = X(rs1) & ~(1 << index)
```

**bclri rd, rs1, shamt:**
```
let index = shamt & (XLEN - 1);
X(rd) = X(rs1) & ~(1 << index)
```

**bext rd, rs1, rs2** Single-Bit Extract (Register)
```
let index = X(rs2) & (XLEN - 1);
X(rd) = (X(rs1) >> index) & 1;
```

**bext rd, rs1, shamt:**
```
let index = shamt & (XLEN - 1);
X(rd) = (X(rs1) >> index) & 1;
```

**binv rd, rs1, rs2:** Single-Bit Invert (Register)

```
let index = X(rs2) & (XLEN - 1);
X(rd) = X(rs1) ^ (1 << index)
```

**binvi rd, rs1, shamt:**
```
let index = shamt & (XLEN - 1);
X(rd) = X(rs1) ^ (1 << index)
```

**bset rd, rs1, rs2:** Single-Bit Set (Register)
```
let index = X(rs2) & (XLEN - 1);
X(rd) = X(rs1) | (1 << index)
```

**bseti rd, rs1, shamt:** 
```
let index = shamt & (XLEN - 1);
X(rd) = X(rs1) | (1 << index)
```
:::


## B_extension.scala

#### FunctionSet
```scala
object ALUFunctions extends ChiselEnum {
  val zero, add, sub, sll, slt, xor, or, and, srl, sra, sltu = Value
  // Zba 
  val sh1add, sh2add, sh3add = Value
  // Zbb
  val andn, orn, xnor, 
      clz, ctz, cpop, 
      max, maxu, min, minu,
      sextb, sexth, zexth,
      rol, ror, rori,
      orcb, rev8  = Value
  // Zbc
  val clmul, clmulh, clmulr = Value
  // Zbs
  val bclr, bclri, bext, bexti, binv, binvi, bset, bseti = Value
}
```

e.g.

### Zba
#### sh1add
```scala
object B_Extension{

  def ShiftRightB(A_in: UInt, bits: UInt): UInt = (A_in >> bits).asUInt
```

### Zbb
#### clz

```scala

  def CountLeadingZeros(A_in: UInt): UInt = PriorityEncoder(Reverse(A_in))
```


### ALU.scala

```scala
// ...
  switch(io.func) {
    // ...
    // Zba
    is(ALUFunctions.sh1add) {
      io.result := B_Extension.ShiftLeftB(io.op1, 1.U)+ io.op2
    }
    // ...
      
    // Zbb
    is(ALUFunctions.clz)  {
      io.result   :=  B_Extension.CountLeadingZeros(io.op1)
    }
    // ...  
```

## Test
Test the execution of pepeline and whether adding extension causes errors.
```
sbt test
```
![image](https://hackmd.io/_uploads/H1VPHRgdJg.png)

## Ref 
[chisel-tutorial](https://github.com/ucb-bar/chisel-tutorial)
[Macbook M1使用vscode+iverilog+gtkwave实现Verilog代码的编译与运行](https://blog.csdn.net/qq_62561876/article/details/133901907)
[如何在Mac OS X上安裝Verilog環境](https://easonchang.com/posts/verilog-on-macosx)
[riscv-mini](https://github.com/ucb-bar/riscv-mini?tab=readme-ov-file)
[RV32I Instruction](https://hackmd.io/@qM_cm68kRSyC_0lissRMNg/Sk1LYH-Syx)
[Pseudocode for instruction semantics](https://five-embeddev.com/riscv-bitmanip/1.0.0/bitmanip.html)
[Lab3: Construct a single-cycle RISC-V CPU with Chisel](https://hackmd.io/@sysprog/r1mlr3I7p#Reference)
["B" Extension for Bit Manipulation, Version 1.0.0](https://github.com/riscv/riscv-isa-manual/blob/main/src/b-st-ext.adoc#insns-andn)
