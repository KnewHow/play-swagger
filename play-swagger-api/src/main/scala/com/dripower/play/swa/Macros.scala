package play.swagger.api

import java.io.FileWriter
import scala.reflect.macros.blackbox._
import org.joda.time._
import play.swagger.route.api.PlayRoute

//构造函数中引入 context 对象
class Macros(val c: Context) {
  import c.universe._

  //宏实现函数，C 表示类型参数
  def api[C: c.WeakTypeTag]() = {
    val controller = c.weakTypeTag[C].tpe
    val ms = weakTypeTag[C].tpe.decls.collect {
      case m: MethodSymbol if !m.isConstructor && isSwaAction(m)  => { // 把构造函数和不是 SwaAction 返回值的方法过滤掉
        m
      }
    }.toList  // 把最终的结果转换为一个包含所有方法的 List 集合
    val apis  = extractApis(controller.toString, ms)
    q"$apis"
  }

  def routes[T: c.WeakTypeTag](a: c.Expr[T]) = {
    // product
    val ms = weakTypeTag[T].tpe.decls.collect {
      case m: MethodSymbol if !m.isConstructor && isSwaAction(m)  => m
    }.toList

    val routes =  ms.map { m =>
      val r = extractRoute(m)
      (r.requestPath) -> genMethod[T](a,m)
    }.toMap

     q"_root_.scala.collection.immutable.Map(..$routes)"

  }

  private  def genMethod[T](a: c.Expr[T], m: MethodSymbol) = {
     q"""
     import _root_.shapeless._
     import _root_.shapeless.syntax.std.function._
     (${a}.$m _).toProduct
      """
  }


  private def extractApis(cname: String, list:List[MethodSymbol]) = {
    import scala.collection.mutable.ListBuffer
    val apis = new ListBuffer[String]()
    for(m <- list) {
      val r = extractRoute(m)
      val api = extractApi(cname, m, r, getActionDescrip(m))
      apis += toJSONStr(api)
    }
    apis.toList
  }

  private def getActionDescrip(m: MethodSymbol):Map[String, Any] = {
    val ans = m.annotations
    if(ans.nonEmpty) {
      val a = ans(0).tree.productElement(1)
      a match {
        case l:List[_] => Map("describe" -> removeStrQuotation(l(0).toString))
        case _         => Map("describe" -> "")
      }
    } else  {
      Map("describe" -> "注解解析错误")
    }
  }

  private def removeStrQuotation(s: String): String = {
    if(s.startsWith("\"") && s.endsWith("\"")) {
      s.substring(1,s.length-1)
    } else {
      s
    }
  }

   private def toJSONStr(api:Map[String,Any],tab:String = " "):String = {
    api.map{
      case (k,v:String) => "\"" + k + "\"" + ":" +  "\"" + v + "\""
      case (k,v:Map[String,Any]) => "\"" + k + "\"" + ":" +toJSONStr(v)
    }.mkString("{", ",", "}")
  }


  private def extractApi(cName: String, m: MethodSymbol,r:PlayRoute,actionDes:Map[String, Any]) = {
    val typeParams = m.returnType.typeArgs
    val req = extractMember(cName, m.toString, typeParams(0))
    val res = extractMember(cName, m.toString, typeParams(1))
    val api:Map[String,Any] = actionDes ++  Map(("route",r.requestPath),("method",r.method),("req",req),("res",res))
    api
  }

  /**
   * 设计思路：
   * 对于基本类型的属性，使用 id: Map{type -> Int, descrip -> xxx} 来描述
   * 对于包装数据类型，如：case class Car(id:Long,logo:String)
   * 使用 car: Map{type -> Map{"logo" -> Map{type -> String,descrip -> xxx}}}
   * 对于 List[基本类型] 如 names: List[String] 使用 names: Map{type -> List[String], descrip -> xxx}
   * 对于 List[包装类型] 如 friends: List[Friend] 使用 friends: List[Friends]: Map{descrip -> xxx, type -> Map{"name" -> Map{type -> String, descrip -> xxx }}}
   */
  private def extractMember(cName: String, mName:String, t:c.universe.Type):Map[String,Any] = {
    if(isList(t)) { // 用于t直接为List类型
      Map(("List",extractMember(cName, mName, t.typeArgs(0))))
    } else {
      t.members.collect {
        case m: MethodSymbol if m.isCaseAccessor =>
          val des = getParameterDes(cName, mName, t.toString, m.toString)
          println(s"describe -> ${des}")
          if(isBasicType(m.returnType)) {
            (m.name.toString, Map("type" -> dealMultiplyName(m.returnType.toString)) ++ des )
          } else if (isList(m.returnType)) { // 用于成员含有List类型
            val t = m.returnType.typeArgs(0)
            if(isBasicType(t)) { // List的基本类型，如List[String]
              (m.name.toString, Map("type" -> m.returnType.toString) ++ des)
            } else {
              (s"${m.name.toString}:${dealMultiplyName(m.returnType.toString)}", Map("type" -> extractMember(cName, mName, t)) ++ des ) // List的对象类型，如List[Person]
            }
          }
          else { //对象包对象的情况
            (m.name.toString, Map("type" -> extractMember(cName, mName, m.returnType)) ++ des)
          }

      }.toMap
    }
  }
  private def isList(t:c.universe.Type):Boolean = {
    val s = t.toString
    val tStr = s.contains("[") match {
      case true => s.substring(0,s.indexOf("["))
      case false => s
    }
    tStr.equals("List")
  }

  /**
   *  判断是否为基本类型，这里把Option类型和Date类型，默认为基本类型
   */
  private def isBasicType(t:c.universe.Type):Boolean = {
    val basicType = List(
      "Long",
      "String",
      "Option",
      "Boolean",
      "Int",
      "Double",
      "Float",
      "Char",
      "Short",
      "Byte",
      "Unit",
      "org.joda.time.DateTime"
    )
    var s = t.toString
    var tStr = if (s.contains("[")) {
      s.substring(0,s.indexOf("["))
    } else {
      s
    }
    basicType.contains(tStr)
  }

  private def extractRoute(m: MethodSymbol):PlayRoute = {
    val method = if (isPostSwaAction(m)) PlayRoute.POST else PlayRoute.GET
    val actionPath = m.fullName
    val requestPath = extractRequestPath(actionPath)
    PlayRoute(
      method,
      requestPath,
    )
  }


  private def dealMultiplyName(name:String):String = {
    name.contains("[") match {
      case true => dealWithBracket(name)
      case false => name.substring(name.lastIndexOf(".") + 1, name.length)
    }
  }

  private def dealWithBracket(s:String):String = {

    def deal() = {
      val c =  s.substring(s.lastIndexOf(".")+1, s.indexOf("]"))
      val pre = s.substring(0,s.indexOf("["))
      s"${pre}[${c}]"
    }
    s.contains(".") match {
      case true => deal()
      case false => s
    }

  }

  private def extractRequestPath(fullName:String):String = {
    val strs = fullName.split("\\.")
    val actionName = strs(strs.length-1)
    val controllerName = strs(strs.length-2)
    val lastPackageName = strs(strs.length-3)
    s"${lastPackageName}-${controllerName}-${actionName}"
  }


  /**
   * 从所有的方法中选择返回值为 SwaAction的方法，因为那才是我们需要的方法
   * 这里使用的是字符串匹配的方式，通过匹配方法返回值是否是 “com.dripower.play.swa.SwaAction”
   * 有点low,最好的方式是通过类型来进行判断，但是目前不会，后期可以优化。
   */
  private def isSwaAction(m: MethodSymbol):Boolean = {
    isPostSwaAction(m) || isGetSwaAction(m)
  }


  private def isPostSwaAction(m: MethodSymbol):Boolean = {
    val returnTypeStr = m.returnType.toString
    val i =  returnTypeStr.indexOf("[")
    if (i == -1){
      false
    } else {
      val swaActionStr = returnTypeStr.substring(0,i)
      if(swaActionStr.equals("com.dripower.play.swa.PostSwaAction")){
        true
      }else {
        false
      }
    }
  }

  private def isGetSwaAction(m: MethodSymbol):Boolean = {
    val returnTypeStr = m.returnType.toString
    val i =  returnTypeStr.indexOf("[")
    if (i == -1){
      false
    } else {
      val swaActionStr = returnTypeStr.substring(0,i)
      if(swaActionStr.equals("com.dripower.play.swa.GetSwaAction")){
        true
      }else {
        false
      }
    }
  }

  private def getPropertiese = {
    import java.io.BufferedInputStream
    import java.io.FileInputStream
    import java.util.Properties
    import java.io.File
    import java.io.BufferedReader
    import java.io.InputStreamReader
    val fis = new FileInputStream(new File("/home/ygh/project/play-swa-demo/server/conf/parameter.properties"))

    val br = new BufferedReader(new InputStreamReader(fis))
    val properties = new Properties()
    properties.load(br)
    properties
  }

  private def getDescByKey(key:String):String = {
    val prop = getPropertiese
    val v = prop.getProperty(key)
    if(v == null) {
      ""
    } else {
      v
    }
  }

  private def getParameterDes(cName: String, mName: String,pcName:String, fieldName: String):Map[String, String] = {
    // println(s"cname -> ${cName}, mname -> ${mName}, pcName -> ${pcName}, fieldName -> ${fieldName}")
    val realCName = cName.split("\\.").last
    val realMName = mName.split(" ").last
    val realPcName = pcName.split("\\.").last
    val realFName = fieldName.split(" ").last
    val key = s"${realCName}.${realMName}.${realPcName}.${realFName}"
    println(s"describe key -> ${key}")
    Map("describe" -> getDescByKey(key))
  }

  def obtainAnnotation[C: c.WeakTypeTag]() = {
    val methodList = weakTypeTag[C].tpe.decls.collect {
      case m: MethodSymbol if !m.isConstructor  => m
    }.toList

    val results =  methodList.collect {
       case m => abtractAnnotation(m)
     }
     q"$results"
  }

  private def abtractAnnotation(m: MethodSymbol) = {
    val typeParams = m.returnType.typeArgs
    if(typeParams.size!=0) {
      val first = typeParams(0)
      val second = typeParams(1)
      val f = first.members.collect {
        case m: MethodSymbol if m isCaseAccessor =>
          getFiledAnnotationInfo(m)
      }
      val s = second.members.collect {
        case m: MethodSymbol if m isCaseAccessor =>
          getFiledAnnotationInfo(m)
      }

      (f ++ s).toString

    } else {
      ""
    }

  }

  private def getFiledAnnotationInfo(m: MethodSymbol) = {
    val annotations = m.annotations
    println(s"method -> ${m}, annotations ->${annotations}")
    s"method -> ${m}, annotations ->${annotations}"
  }

}
