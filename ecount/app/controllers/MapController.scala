package controllers

import persistence.MapStore
import persistence.PersistenceContext._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.libs.json.Json

object MapController extends Controller {

  def countyBounds() = Action.async {

    def getAndGroupCounties = {
      withConnection { implicit conn =>
        MapStore.getCountyBounds().map(ctd => {
          Json.obj(
            "type" -> "Feature",
            "geometry" ->  Json.parse(ctd.geom),
            "properties" -> Json.obj(
              "id" -> ctd.id
            )
          )
        })
      }
    }

    val groupedCounties = scala.concurrent.Future { getAndGroupCounties }
    groupedCounties.map(i => Ok(Json.obj(
      "type" -> "FeatureCollection",
      "features" -> Json.toJson(i)
    )))
  }
  // loads ED coordinates in Geo-Json format for a county by countyId
  def electoralDivisions(countyId: Long) = Action.async {

    def getEDByCountyId =
      withConnection { implicit conn =>
        MapStore.getElectoralDivisions(countyId).map(ed => {
          Json.obj(
            "type" -> "Feature",
            "geometry" ->  Json.parse(ed.geometry),
            "properties" -> Json.obj(
              "id" -> ed.id,
              "name" -> ed.label,
              "county" -> ed.county
            )
          )
        })
      }

    val edByCounty = scala.concurrent.Future { getEDByCountyId }

    edByCounty.map{ i =>
      Ok( Json.obj(
        "type" -> "FeatureCollection",
        "features" -> Json.toJson(i)
        )
      )
    }
  }

  // load county statistics in interactive map view
  def loadCountyStats(countyId: Long) = TODO
}
