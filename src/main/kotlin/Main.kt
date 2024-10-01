import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.util.zip.Inflater
import kotlin.io.path.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: your_program.sh <command> [<args>]")
        exitProcess(1)
    }

    when(args[0]) {
        "init" -> init()
        "cat-file" -> catFile(args)
        else -> {
            println("Unknown command: ${args[0]}")
            exitProcess(1)
        }
    }
}

fun init() {
    val gitDir = File(".git")
    gitDir.mkdir()
    File(gitDir, "objects").mkdir()
    File(gitDir, "refs").mkdir()
    File(gitDir, "HEAD").writeText("ref: refs/heads/master\n")
    println("Initialized git directory")
}

fun catFile(args: Array<String>) {
    if (args.size != 3 || args[1] != "-p") {
        println("Usage ${args[0]} -p <blob_sha>")
        exitProcess(1)
    }

    val objectFile = Path(
        ".git",
        "objects",
        args[2].substring(0, 2),
        args[2].substring(2)
    ).toFile()

    val decompressed = decompress(objectFile)
    val stripped = decompressed.dropWhile { it != '\u0000' }.drop(1)
    println(stripped)
}

fun decompress(file: File): String {
    val rawBytes = file.inputStream().readAllBytes()
    val buffer = ByteArray(4096)
    val stream = ByteArrayOutputStream()

    val inflater = Inflater().apply { setInput(rawBytes) }
    while (!inflater.finished()) {
        val len = inflater.inflate(buffer)
        stream.write(buffer, 0, len)
    }
    inflater.end()

    return String(stream.toByteArray(), Charset.defaultCharset())
}

fun fail(command: String) {
    println("Unknown command: $command")
    exitProcess(1)
}
