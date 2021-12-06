import scala.annotation.switch
import scala.{Array => $}
object TestScala{
  def main(args: $[String]): Unit = {
    val table = $(
      $(5, 3, 0,  0, 7, 0,  0, 0, 0),
      $(6, 0, 0,  1, 9, 5,  0, 0, 0),
      $(0, 9, 8,  0, 0, 0,  0, 6, 0),
      $(8, 0, 0,  0, 6, 0,  0, 0, 3),
      $(4, 0, 0,  8, 0, 3,  0, 0, 1),
      $(7, 0, 0,  0, 2, 0,  0, 0, 6),
      $(0, 6, 0,  0, 0, 0,  2, 8, 0),
      $(0, 0, 0,  4, 1, 9,  0, 0, 5),
      $(0, 0, 0, 0, 8, 0,  0, 7, 9))
    var sudoku = new Sudoku()
    sudoku.parcours_12(table) match {
      case Some(res) => println("12: \n"+ res.map( _.mkString(" ") ).mkString("\n"))
      case None => println("pas de solution")
    }
  }
}
class Sudoku() {

  def parcours_1(i_ : Int = 0): Tuple2[Int, Int] = {
    val valeur = i_
    var valueRow = 0
    var valueLine = 0
    var result = (0, 0)
    valeur match {
      case a if 9 to 17 contains a => valueLine = 1
      case a if 18 to 26 contains a => valueLine = 2
      case a if 27 to 35 contains a => valueLine = 3
      case a if 36 to 44 contains a => valueLine = 4
      case a if 45 to 53 contains a => valueLine = 5
      case a if 54 to 62 contains a => valueLine = 6
      case a if 63 to 71 contains a => valueLine = 7
      case a if 72 to 81 contains a => valueLine = 8
      case _ => valueLine = 0 // FirstLine
    }
    valueRow = valeur - (9 * valueLine)
    result = (valueLine, valueRow)
    return result
  }
  def parcours_12(t_ : Array[Array[Int]], i_  : Int = 0) : Option[Array[Array[Int]]] = {
    var index = i_
    var currentSquare = t_
    val (x, y): (Int, Int) = parcours_1(index)
    (x, y) match {
      case(8,9) =>{
        print("on a tout fait")
        return  Some(t_)
      }
      case(x,y) if t_(x)(y) != 0 => parcours_12(t_, index +1)
      case(x,y) => {
        def nombresDejaPris(j_ : Int) : List[Int] = {
          val valeurCase = j_
          var result = List[Int]()
          val ligne = (0 until 9).map(v =>   result =currentSquare(x)(v)::result)
          val colonne =(0 until 9).map(v =>  result =currentSquare(v)(y)::result)
          val colonneXY = (0 until 9).map(v =>
            v match {
            case a if 0 to 2 contains a =>
            result = currentSquare((3 * (x / 3)))(v + (3 * (y / 3))) :: result
            case a if 3 to 5 contains a =>
            result = currentSquare((3 * (x / 3)) + 1)(v - 3 + (3 * (y / 3))) :: result
            case a if 5 to 8 contains a =>
            result = currentSquare((3 * (x / 3)) + 2)(v - 6 + (3 * (y / 3))) :: result
            })
          return result
        }
        val dejaPris = (0 until 9).flatMap(nombresDejaPris).toSet
        val aEssayer = Set(1,2,3,4,5,6,7,8,9) diff dejaPris
        val aEssayerT = aEssayer.toSeq
        println("XY"+x+y)
        def placer(j_ : Int) = parcours_12(currentSquare.updated(x, currentSquare(x).updated(y,j_)), i_ +1)

        if (aEssayerT.isEmpty) None // pas de solution (rien à essayer)
         else {
          println("a essa"+aEssayerT)
          val ts = aEssayerT.map(placer).filterNot(_ == None)
          if (ts.length > 0) ts(0) else None
        }
      }
    }
  }
}
