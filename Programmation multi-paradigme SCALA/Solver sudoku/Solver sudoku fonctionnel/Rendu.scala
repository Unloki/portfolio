1)
import scala.{Array => $}
2)
 print(table.map(_.mkString(" ")).mkString(" \n"))
3)
 val ntable = table.updated(3, table(3).updated(4,9))
 print(ntable.map(_.mkString(" ")).mkString(" \n"))   

4)
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
  // V1 parcours2
  def parcours_2(i_ : Int = 0): Unit = {
    val XY = parcours_1(i_)
    val x = XY._1
    val y = XY._2
    println(i_ +" ->"+XY)
    if(i_ < 80){
      parcours_2(i_ + 1)
    }

  }

  def parcours_2(i_ : Int = 0): Unit = {
    val (x,y) : (Int,Int) = parcours_1(i_)
    (x, y) match {
      case (8, 8) => println("derniere case")
      case (x, y) => {
        println(x + " " + y)
        parcours_2(i_ + 1)
      }
    }
  }


  def parcours_3(i_ : Int = 0) {
    val (x,y) : (Int,Int) = parcours_1(i_)
    (x, y) match {
      case (8,9) => println("on arrete, cette case ne doit pas être analysée")
      case (x, y) => {
        println(x + " " + y)
        parcours_3(i_ + 1)
      }
    }
  }
  
val ligne3 = (0 until 9).map(v =>   (3, v))
val colonne3 =(0 until 9).map(v =>  (v, 3) )
val x = 0;
val y = 0;
val colonneXY = (0 until 9).map(v =>
      v match {
        case a if 0 to 2 contains a =>
          ((3 * (x / 3)), v+(3 * (y / 3)))
        case a if 3 to 5 contains a =>
          ((3 * (x / 3)) +1, v-3+(3 * (y / 3)))
        case a if 5 to 8 contains a =>
          ((3 * (x / 3)) +2, v-6+(3 * (y / 3)))
      }
      )
      
      
def parcours_5(i_ : Int = 0) {
    val (x, y): (Int, Int) = parcours_1(i_)
    (x, y) match {
      case (8, 9) => println("stop ! une case de trop ")
      case (x, y) => {
        println(x + " " + y)
        def indicesAVoir(j_ : Int): List[Tuple2[Int, Int]] = {
        val valeurCase = j_
        var result = List[Tuple2[Int,Int]]()
        val ligne = (0 until 9).map(v =>   result =(x, v)::result)
        val colonne =(0 until 9).map(v =>  result =(v, y)::result)
        val colonneXY = (0 until 9).map(v =>
	      v match {
		case a if 0 to 2 contains a =>
		  result =((3 * (x / 3)), v+(3 * (y / 3)))::result
		case a if 3 to 5 contains a =>
		  result =((3 * (x / 3)) +1, v-3+(3 * (y / 3)))::result
		case a if 5 to 8 contains a =>
		  result =((3 * (x / 3)) +2, v-6+(3 * (y / 3)))::result
	      })
          return result
      }

        println((0 until 9).flatMap(indicesAVoir))
        parcours_5(i_ + 1)
      }
    }
  
  }
  
  def parcours_7(i_ : Int = 0) {
    val (x, y): (Int, Int) = parcours_1(i_)
    (x, y) match {
      case (8, 9) => println("stop")
      case (x, y) => {
        println(x + " " + y)

        def nombresDejaPris(j_ : Int): List[Int] = {
          val valeurCase = j_
          var result = List[Int]()
          val ligne = (0 until 9).map(v => result = modele(x)(v) :: result)
          val colonne = (0 until 9).map(v => result = modele(v)(y) :: result)
          val colonneXY = (0 until 9).map(v =>
            v match {
              case a if 0 to 2 contains a =>
                result = modele((3 * (x / 3)))(v + (3 * (y / 3))) :: result
              case a if 3 to 5 contains a =>
                result = modele((3 * (x / 3)) + 1)(v - 3 + (3 * (y / 3))) :: result
              case a if 5 to 8 contains a =>
                result = modele((3 * (x / 3)) + 2)(v - 6 + (3 * (y / 3))) :: result
            })
          return result
        }

        println("deja pris " + (0 until 9).flatMap(nombresDejaPris).toSet)
        parcours_7(i_ + 1)
      }
    }
  }
  
  def parcours_9(i_ : Int = 0) {
    val (x, y): (Int, Int) = parcours_1(i_)
    (x, y) match {
      case(8,9) => println("stop")
      case(x,y) => {
        println(x + " " + y)
        def nombresDejaPris(j_ : Int) : List[Int] = {
          val valeurCase = j_
          var result = List[Int]()
          val ligne = (0 until 9).map(v =>   result =modele(x)(v)::result)
          val colonne =(0 until 9).map(v =>  result =modele(v)(y)::result)
          val colonneXY = (0 until 9).map(v =>
             v match {
              case a if 0 to 2 contains a =>
                result = modele((3 * (x / 3)))(v + (3 * (y / 3))) :: result
              case a if 3 to 5 contains a =>
                result = modele((3 * (x / 3)) + 1)(v - 3 + (3 * (y / 3))) :: result
              case a if 5 to 8 contains a =>
                result = modele((3 * (x / 3)) + 2)(v - 6 + (3 * (y / 3))) :: result
            })
          return result
        }
        val dejaPris = (0 until 9).flatMap(nombresDejaPris).toSet
        println("dejaPris : "+dejaPris)
        // 1fst TRY :  val aEssayer = (0 until 9).map(v =>  if(!dejaPris.contains(v)) v)
        val aEssayer = Set(0,1,2,3,4,5,6,7,8,9) diff dejaPris
        println("a essayer : "+aEssayer)
        parcours_9(i_ +1)
      }
    }
  }
  
  def parcours_11(t_ : Array[Array[Int]], i_  : Int = 0) : Option[Array[Array[Int]]] = {
    var currentSquare = t_
    val (x, y): (Int, Int) = parcours_1(i_)
    (x, y) match {
      case(8,9) => return  Some(t_)
      case(x,y) => {
        println(x + " " + y)
        def nombresDejaPris(j_ : Int) : List[Int] = {
          val valeurCase = j_
          var result = List[Int]()
          val ligne = (0 until 9).map(v =>   result =currentSquare(x)(v)::result)
          val colonne =(0 until 9).map(v =>  result =currentSquare(v)(y)::result)
          val colonneXY = (0 until 9).map(v =>
             v match {
              case a if 0 to 2 contains a =>
                result = modele((3 * (x / 3)))(v + (3 * (y / 3))) :: result
              case a if 3 to 5 contains a =>
                result = modele((3 * (x / 3)) + 1)(v - 3 + (3 * (y / 3))) :: result
              case a if 5 to 8 contains a =>
                result = modele((3 * (x / 3)) + 2)(v - 6 + (3 * (y / 3))) :: result
            })
          return result
        }
        val dejaPris = (0 until 9).flatMap(nombresDejaPris).toSet
        println("dejaPris : "+dejaPris)
        // 1fst TRY :  val aEssayer = (0 until 9).map(v =>  if(!dejaPris.contains(v)) v)
        val aEssayer = Set(0,1,2,3,4,5,6,7,8,9) diff dejaPris
        println("a essayer : "+aEssayer)
        val aEssayerTemp = aEssayer.toSeq
        val tempTable = currentSquare.updated(x, currentSquare(x).updated(y,i_))
        parcours_11(tempTable, i_ +1)
      }
    }
  }
    
