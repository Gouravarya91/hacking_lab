val MAX_LINES = 1000
val current = (1..1500).toList()
val newLines = current.takeLast(MAX_LINES)
println(newLines.size)
