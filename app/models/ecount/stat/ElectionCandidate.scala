package models.ecount.stat

class ElectionCandidate {
   var id:Int = _
   var name:String = _
}

object ElectionCandidate {
  def apply(ed:ElectionCandidate) =
    Some(ed.id, ed.name)
}
