package models

class CountyElectoralDivision {
  var id:Long = _
  var label:String = _
  var county:String = _
  var geometry:String = _
}


object CountyElectoralDivision {

  def apply(id:Long, label:String, county:String, geom:String) {

    val ed = new CountyElectoralDivision
    ed.id = id
    ed.label = label
    ed.county = county
    ed.geometry = geom
  }

  def unapply(ed:CountyElectoralDivision) {
    Some(ed.id, ed.label, ed.county, ed.geometry)
  }
}