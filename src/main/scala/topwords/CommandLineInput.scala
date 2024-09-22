package topwords
import org.rogach.scallop._

class CommandLineInput(arguments: Seq[String]) extends ScallopConf(arguments) {

    //Default values 
    val CloudSizeDefault = 1000
    val MinLengthDefault = 6
    val WindowSizeDefault = 10

    //Actual command line flags. Supports short and long form as described in README.MD
    val CloudSize = opt[Int](name = "cloud-size", short = 'c', default = Some(CloudSizeDefault), validate = (_ >  0))
    val MinLength = opt[Int](name = "length-at-least", short = 'l', default = Some(MinLengthDefault), validate = (_ >  0))
    val WindowSize = opt[Int](name = "window-size", short = 'w', default = Some(WindowSizeDefault), validate = (_ >  0))
    verify()
}

//Scallop is a third party command line tool. Using a suitable third-party library 
//for command line parsing is one of the listed static requirements. This one
//seems to be popular and suitable.

//Documentation: https://github.com/scallop/scallop/wiki
