import scala.Array.ofDim
import scala.reflect.ClassTag

object StartEchequier{
  def main(args: Array[String]): Unit = {
    val echiquier = new Echiquier[PieceCol]()
    echiquier((0, 6)) = Cavalier()   // indice : utilisez un object Cavalier et un apply()
    echiquier((3, 5)) = Dame()
    echiquier((6, 3)) = Fou()
    echiquier((5, 2)) = Pion()
    echiquier((5, 2)) = Rien() // on vide la case
    print(echiquier)
    println(echiquier(5,2))
    println(echiquier(3,5))
  }
}

trait Piece{
  def lengthP: Int
}

class Echiquier[Piece: ClassTag](cote_ : Int){
  private var plateau = ofDim[Option[Piece]](cote_,cote_)
  for( i <- 0 to cote_ -1 ; j <- 0 to cote_ -1) this.vider(i,j)

  def getNumberCote(): Int ={
    return cote_
  }

  def apply(Tuple2: (Int, Int)): Option[Piece] = {
    val Some(combien) = this.plateau(Tuple2._1)(Tuple2._2)
    if(combien.asInstanceOf[PieceCol].etiquette == ""){
      None;
    }else{
      this.plateau(Tuple2._1)(Tuple2._2)
    }
  }

  def this() {
    this(8);
    println("\n Pas de nombre de côté précisé !\n")
  }
  def PlacerEn(piece_ : Piece, x_ : Int, y_ : Int): Unit ={
    var valeur:Option[Piece] = Option(piece_);
    val Some(combien) = valeur;
    this.plateau(x_)(y_) = valeur;
  }
  def update(coupleXY_ :Tuple2[Int,  Int], piece_ : Piece): Unit ={
    var valeur:Option[Piece] = Option(piece_);
    if(coupleXY_._1 < cote_ && coupleXY_._2 < cote_){
      this.plateau(coupleXY_._1)(coupleXY_._2) = valeur;
    }else{
      System.out.println("Erreur: Piéce en dehors de l'echequier");
    }
  }
  def vider(x_ : Int, y_ : Int): Unit ={
    this.plateau(x_)(y_) = None;
  }

  override def toString(): String = {
    var AllRow: String = "";
    AllRow = PrintLigneWithNumber();
    //ALL BLUE
    for( i <- 0 to cote_ -1) {

      // LIGNE 1
      AllRow = AllRow +SimpleLigne();
      // LIGNE 2
      AllRow = AllRow +GameLigne(i);
    }
    //LAST LIGNE
    AllRow = AllRow + LastLigne();
    return AllRow;
  }

  def PrintLigneWithNumber(): String ={
    var tempLine0: String = "";
    for( i <- 0 to cote_ -1) {
      if (i == 0) tempLine0 = "    "
      tempLine0 = tempLine0 + "   "+i+"   ";
    }
    return tempLine0 + "\n";
  }
  def SimpleLigne(): String ={
    var tempLine0: String = "";
    for( i <- 0 to cote_ -1) {
      if(i == 0) tempLine0 = "   " +Ansi.RESET +Ansi.BLUE_B + "  "
      tempLine0 = tempLine0 + Ansi.BLUE_B + "     "+Ansi.BLUE_B + "  ";
      if(i == cote_ -1){
        tempLine0 =  tempLine0 + " " +Ansi.RESET;
      }
    }
    return tempLine0 +"\n";
  }
  def GameLigne(currentCol_ : Int): String ={
    var tempLine: String = "";
    for( i <- 0 to cote_ -1) {
      if (i == 0) tempLine = " "+currentCol_ + " " +Ansi.RESET + Ansi.BLUE_B + "  "
      if(!this.plateau(currentCol_)(i).isEmpty){
        val Some(etiquettePiece) = this.plateau(currentCol_)(i)
        val numberSpacecToAdd = 5 - etiquettePiece.asInstanceOf[PieceCol].lengthP;
        if(numberSpacecToAdd > 0){
          val spaceToAdd = GenerateEspace(numberSpacecToAdd);
          tempLine = tempLine + Ansi.RESET + etiquettePiece + spaceToAdd + Ansi.BLUE_B + "  ";
        }else{
          tempLine = tempLine + Ansi.RESET + etiquettePiece + Ansi.BLUE_B + "  ";
        }
      }else{
        tempLine = tempLine + Ansi.RESET + "     " + Ansi.BLUE_B + "  ";
      }
      if (i == cote_ - 1) {
        tempLine = tempLine + " " + Ansi.RESET;
      }
    }
    return tempLine +"\n";
  }
  def LastLigne(): String ={
    var tempLineLast: String = "";
    for( i <- 0 to cote_ -1) {
      if (i == 0) tempLineLast = "   " +Ansi.RESET +Ansi.BLUE_B + "  "
      tempLineLast = tempLineLast + Ansi.BLUE_B + "     " + Ansi.BLUE_B + "  ";
      if (i == cote_ - 1) {
        tempLineLast = tempLineLast + " " + Ansi.RESET;
      }
    }
    return tempLineLast +"\n";
  }
  def GenerateEspace(i_ : Int): String ={
    var tempLineLast: String = "";
    for( i <- 0 to i_ -1) {
      tempLineLast = tempLineLast + " ";
    }
    return tempLineLast;
  }

}

case class PieceCol(etiquette_ :  String,  codeAnsi_ :  String) extends  Piece{
  val codeAnsi = codeAnsi_
  val etiquette = etiquette_

  override def toString(): String = {
    return codeAnsi+ etiquette +Ansi.RESET
  }

  override def lengthP: Int = etiquette.length
}
object Cavalier {
  // ALTERNATE CONSTRUCTOR #1 (without numClicks)
  def apply():
  PieceCol = {
    PieceCol("Caval", Ansi.YELLOW)
  }
}
object Dame {
  // ALTERNATE CONSTRUCTOR #1 (without numClicks)
  def apply():
  PieceCol = {
    PieceCol("Dame", Ansi.RED)
  }
}
object Fou {
  // ALTERNATE CONSTRUCTOR #1 (without numClicks)
  def apply():
  PieceCol = {
    PieceCol("Fou", Ansi.GREEN)
  }
}
object Pion {
  // ALTERNATE CONSTRUCTOR #1 (without numClicks)
  def apply():
  PieceCol = {
    PieceCol("Pion",  Ansi.CYAN)
  }
}
object Rien {
  // ALTERNATE CONSTRUCTOR #1 (without numClicks)
  def apply():
  PieceCol = {
    PieceCol("", Ansi.RESET)
  }
}


object PieceCol {

  // ALTERNATE CONSTRUCTOR #1 (without numClicks)
  def apply(etiquette_ : String):
  PieceCol = {
    PieceCol(etiquette_, Ansi.WHITE+Ansi.BLACK_B)
  }
}

object Ansi {
  def reset() {
    println(Ansi.RESET)
  }

  val RESET: String ="\u001B[0m"

  val BLACK_B: String = "\u001B[40m"

  val RED: String = "\u001B[31m"

  val RED_B: String = "\u001B[41m"

  val GREEN: String = "\u001B[32m"

  val GREEN_B: String = "\u001B[42m"

  val YELLOW: String = "\u001B[33m"

  val YELLOW_B: String = "\u001B[43m"

  val BLUE: String = "\u001B[34m"

  val BLUE_B: String = "\u001B[44m"

  val MAGENTA: String = "\u001B[35m"

  val MAGENTA_B: String = "\u001B[45m"

  val CYAN: String = "\u001B[36m"

  val CYAN_B: String = "\u001B[46m"

  val WHITE: String = "\u001B[37m"

  val WHITE_B: String = "\u001B[47m"
}
