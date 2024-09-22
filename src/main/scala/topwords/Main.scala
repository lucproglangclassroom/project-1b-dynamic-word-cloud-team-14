package topwords

object Main:

  def main(args: Array[String]) = 
    println("Hello scalatest!")
    println(s"Today's date is ${java.time.LocalDate.now}.")

    val cmdArgs = new CommandLineInput(args)
    println("cloud size = " + cmdArgs.CloudSize()) //just a test/demonstration
    //command line values can be accessed using things like "cmdArgs.CloudSize()". 
    //For more info, see CommandLineInput.scala

end Main
