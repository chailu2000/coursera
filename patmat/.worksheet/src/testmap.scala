object testmap {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(60); 
  println("Welcome to the Scala worksheet");$skip(55); 
      val a = List(('a', List(0)),('b',List(0,1,0,1)));System.out.println("""a  : List[(Char, List[Int])] = """ + $show(a ));$skip(55); 
      val b = List(('a', List(0)),('b',List(0,1,0,1)));System.out.println("""b  : List[(Char, List[Int])] = """ + $show(b ));$skip(47); 
 
      
      
            val mapa = a.toMap;System.out.println("""mapa  : scala.collection.immutable.Map[Char,List[Int]] = """ + $show(mapa ));$skip(23); 
    val mapb = b.toMap;System.out.println("""mapb  : scala.collection.immutable.Map[Char,List[Int]] = """ + $show(mapb ));$skip(28); 
    val mapd = mapa ++ mapb;System.out.println("""mapd  : scala.collection.immutable.Map[Char,List[Int]] = """ + $show(mapd ));$skip(26); 
    val value = mapb('a');System.out.println("""value  : List[Int] = """ + $show(value ));$skip(63); 
    var mapc = scala.collection.mutable.Map[Char, List[Int]]();System.out.println("""mapc  : scala.collection.mutable.Map[Char,List[Int]] = """ + $show(mapc ));$skip(75); 
    
    for (k <- mapa.keys) {
       mapc += (k-> (mapa(k) ::: mapb(k)))
    };$skip(22); val res$0 = 
    mapc.toList;System.out.println("""res0: List[(Char, List[Int])] = """ + $show(res$0))}
}
