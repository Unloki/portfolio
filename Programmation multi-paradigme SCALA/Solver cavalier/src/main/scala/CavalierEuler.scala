import scala.Array.ofDim
import scala.collection.mutable.Stack

object StartCavalier{
  def main(args: Array[String]): Unit = {
    val euler = new CavalierEuler()
    time {euler.controleur(0,0)}
    //println(euler)
  }
  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    val miliTime = (t1 - t0) / 1000000
    println("Elapsed time: " + miliTime + "ms")
    result
  }

}
class CavalierEuler(cote_ : Int){
  def this() {
    this(8);
    println("\n Pas de nombre de côté précisé !\n")
  }

  var vue = new Echiquier[PieceCol](cote_)
  var N = vue.getNumberCote()


  val modele = ofDim[Int](N,N)



  def controleur(xy_ : Tuple2[Int, Int]): Unit = {
    var heuristique = Array.fill(cote_, cote_)(0)
    trouvePositions(xy_, 1)
    synchroniseVueAuModele()
    for (x <- 0 until N) {
      for (y <- 0 until N) {
        modele(x)(y) = -1
      }
    }

    def trouvePositions(xy_ : Tuple2[Int, Int], etape_ : Int): Boolean = {
      var nmb = cote_
      var etapeCourante = etape_
      var pos : List[Tuple2[Int,Int]] = Nil
      var heuristique = Array[Array[Int]]()
      this.modele(xy_._1)(xy_._2) = etapeCourante
      if(etape_ == (nmb*nmb)) {
        return true
      }
      heuristique = Array.tabulate(cote_,cote_) { case(x,y) => trouveDeplacementsCavalier((x,y)).size}
      pos = trouveDeplacementsCavalier(xy_)

      for (newTuple <- pos.sortBy((xy : Tuple2[Int,Int]) => heuristique(xy._1)(xy._2))) {
          if (trouvePositions((newTuple._1, newTuple._2), etapeCourante + 1))
            return true
      }
      this.modele(xy_._1)(xy_._2) = -1
      return false     
    }
   
    def isSafe(x: Int, y: Int, sol: Array[Array[Int]]): Boolean = {
      x >= 0 && x < N && y >= 0 && y < N && sol(x)(y) == 0
    }


    def trouveDeplacementsCavalier(xy_ : Tuple2[Int, Int]): List[Tuple2[Int, Int]] = {
      var next_x = 0
      var next_y = 0
      val xMove = Array(2, 1, -1, -2, -2, -1, 1, 2)
      val yMove = Array(1, 2, 2, 1, -1, -2, -2, -1)
      var k = 0
      var deplacementReturn: List[Tuple2[Int, Int]] = List()

      while ( {
        k < 8
      }) {
        next_x = xy_._1 + xMove(k)
        next_y = xy_._2 + yMove(k)
        if (isSafe(next_x, next_y, modele)) {
          val addDeplacementList: List[Tuple2[Int, Int]] = List((next_x, next_y))
          deplacementReturn = deplacementReturn ::: addDeplacementList
        }
        k += 1
      }

      deplacementReturn = deplacementReturn.filter(x => (x._1 < vue.getNumberCote()
        && x._2 < vue.getNumberCote()
        && x._1 >= 0
        && x._2 >= 0
        ))


      return deplacementReturn
    }


    def synchroniseVueAuModele(): Unit = {
      vue((0, 0)) = new PieceCol("0" , Ansi.CYAN)
      //println("result "+visited.length)
      for(i<-0 to N-1; j<-0 until N)
      {
        //print(i, j)
        //Accessing the elements

        //println("result "+visited(i)(j)+"" + i +" " +j)
        vue((i, j)) = new PieceCol(this.modele(i)(j).toString() , Ansi.CYAN);
      }
      print(vue)
    }
  }
}
