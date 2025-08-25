package tacky

data class TackyResult(
    val instructions: List<TackyInstruction>,
    val resultVal: TackyVal?
    // can be null for statements
)
