
package riscv.core

import chisel3._
import chisel3.util._

// object ZbaFunctions extends ChiselEnum {
//   val sh1add, sh2add, sh3add = Value
// }

// object ZbbFunctions extends ChiselEnum {
//   val   andn, orn, xnor, 
//         clz, ctz, cpop, 
//         max, maxu, min, minu,
//         sextb, sexth, zexth,
//         rol, ror, rori,
//         orcb, rev8  = Value
// }

// object ZbcFunctions extends ChiselEnum {
//   val clmul, clmulh, clmulr = Value
// }

// object ZbsFunctions extends ChiselEnum {
//   val bclr, bclri, bext, bexti, binv, binvi, bset, bseti = Value
// }

object B_Extension{

  def ShiftRightB(A_in: UInt, bits: UInt): UInt = (A_in >> bits).asUInt
  def ShiftLeftB(A_in: UInt, bits: UInt): UInt = (A_in << bits).asUInt

  def CountLeadingZeros(A_in: UInt): UInt = PriorityEncoder(Reverse(A_in))

}


// Implementing n bit shift right
class ShiftRightB(N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W))
    val bits = Input(UInt(log2Ceil(N).W))
    val A_out = Output(UInt(N.W))
  })

    val A_temp  = (io.A_in >> io.bits).asUInt

    io.A_out  := A_temp
}


// Implementing n bit shift left
class ShiftLeftB(N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W))
    val bits = Input(UInt(log2Ceil(N).W))
    val A_out = Output(UInt(N.W))
  })

    val A_temp  = (io.A_in << io.bits).asUInt

    io.A_out  := A_temp
}


// // Implementing n bits bitwise inversion
// class InvertBits(N: Int) extends Module {
//   val io = IO(new Bundle {
//     val A_in = Input(UInt(N.W))
//     val A_out = Output(UInt(N.W))
//   })

//     val A_temp  = (~(io.A_in)).asUInt

//     io.A_out  := A_temp
// }

// // Implementing n bits bitwise AND
// class ANDBits(N: Int) extends Module {
//   val io = IO(new Bundle {
//     val A_in = Input(UInt(N.W))
//     val B_in = Input(UInt(N.W))
//     val and = Output(UInt(N.W))
//   })

//     val and_temp  = (io.A_in & io.B_in).asUInt

//     io.and  := and_temp
// }

// // Implementing n bits bitwise OR
// class ORBits(N: Int) extends Module {
//   val io = IO(new Bundle {
//     val A_in = Input(UInt(N.W))
//     val B_in = Input(UInt(N.W))
//     val or = Output(UInt(N.W))
//   })

//     val or_temp  = (io.A_in | io.B_in).asUInt

//     io.or  := or_temp
// }

// // Implementing n bits bitwise XOR
// class XORBits(N: Int) extends Module {
//   val io = IO(new Bundle {
//     val A_in = Input(UInt(N.W))
//     val B_in = Input(UInt(N.W))
//     val xor = Output(UInt(N.W))
//   })

//     val xor_temp  = (io.A_in ^ io.B_in).asUInt

//     io.xor  := xor_temp
// }

// Implementing n bits leading zero counter CLZ
class CountLeadingZeros(N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W)) // Input signal
    val A_out = Output(UInt(log2Ceil(N+1).W)) // Output leading zero count
  })

  // Reverse the bits and use PriorityEncoder to find leading zeros
  val reversed = Reverse(io.A_in) // Reverse the input bits so that MSB is at LSB
  val leadingZeros = PriorityEncoder(reversed) // PriorityEncoder finds the first 1 by counting from LSB
 
  // If the input is all zeros, set the result to N (32 for 32-bit input)
  io.A_out := Mux(io.A_in === 0.U, N.U, leadingZeros)
}

// Counting trailing zeros using priority encoder
class CountTrailingZeros(N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W))       // Input signal
    val A_out = Output(UInt(log2Ceil(N+1).W)) // Output trailing zero count
  })

  // PriorityEncoder finds the first '1', which corresponds to the trailing zero count
  val trailingZeros = PriorityEncoder(io.A_in)

  // If the input is all zeros, set the result to N (word size)
  io.A_out := Mux(io.A_in === 0.U, N.U, trailingZeros)
}

// Implementing count population instruction counting number of true or 1 bits
// PopCount(input)

// Finding maximum signed operand between A_in and B_in
// io.max := Mux(io.ra > io.rb, io.ra, io.rb)


// Finding minimum signed operand between rA_in and B_in
// io.max := Mux(io.ra < io.rb, io.ra, io.rb)


// Finding minimum Unsigned operand between A_in and B_in
// io.min := Mux(io.ra < io.rb, io.ra, io.rb).asUInt


// Sign extention after most significant bit of least significant byte (7,0) all the way upto the most significant bit (32 bit) of operand rs1
class SextB (N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W))  
    val A_out = Output(UInt(N.W)) 
  })

  // Extract the least significant byte
  val byte = io.A_in(7, 0)

  // Sign-extend the byte to 32 bits
  val signExtended = Cat(Fill(24, byte(7)), byte) // Fill the upper 24 bits with the MSB (bit 7)

  // Output the result
  io.A_out := signExtended
}

// Sign extention after most significant bit of least significant halfword (15,0) all the way upto the most significant bit (32 bit) of operand rs1
class SextH (N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W))  
    val A_out = Output(UInt(N.W)) 
  })

  // Extract the least significant half word
  val halfword = io.A_in(15, 0)

  // Sign-extend the byte to 32 bits
  val signExtendedH = Cat(Fill(16, halfword(15)), halfword) // Fill the upper 24 bits with the MSB (bit 7)

  // Output the result
  io.A_out := signExtendedH
}

// Zero extention after most significant bit of least significant halfword (15,0) all the way upto the most significant bit (32 bit) of operand rs1
class ZextH(N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W)) 
    val A_out = Output(UInt(N.W)) 
  })

  // Extract the least-significant 16 bits and zero-extend to N bits (32)
  io.A_out := io.A_in(15, 0).asUInt & Fill(N, 1.U(1.W))
}

// Taking OR of every byte individually and then combining the result
class OrcB(N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W)) 
    val A_out = Output(UInt(N.W))
  })

  // Number of bytes in the input register
  val numBytes = N / 8
  val inputBytes = Wire(Vec(numBytes, UInt(8.W)))

  // Extract each byte
  for (i <- 0 until numBytes) {
    inputBytes(i) := io.A_in(8 * (i + 1) - 1, 8 * i)
  }

  // OR combine each byte
  val outputBytes = Wire(Vec(numBytes, UInt(8.W)))

  for (i <- 0 until numBytes) {
    // Check if any bit in the byte is set
    when(inputBytes(i) === 0.U) {
      outputBytes(i) := 0x00.U
    }.otherwise {
      outputBytes(i) := 0xFF.U
    }
  }
  
  // Combine the processed bytes into the output register
  io.A_out := outputBytes.asUInt
}

// Reversing the order of bytes
class Rev8(N: Int) extends Module {
  val io = IO(new Bundle {
    val A_in = Input(UInt(N.W))  
    val A_out = Output(UInt(N.W)) 
  })

  // Calculate the number of bytes in the register
  val numBytes = N / 8

  // Reverse the bytes
  val ByteOrder = Wire(Vec(numBytes, UInt(8.W)))
  for (i <- 0 until numBytes) {
    ByteOrder(i) := io.A_in((i + 1) * 8 - 1, i * 8) // Extract byte i
  }

  // Concatenate reversed bytes and assign to ReverseOrder
  val ReverseOrder = ByteOrder.reverse

  // Convert to Vec and then to UInt
  io.A_out := VecInit(ReverseOrder).asUInt 
}
