package persistence.ecount

import org.mybatis.scala.mapping._

import models.ecount.map.{CountyElectoralDivision, CountyGeom}
import models.ecount.stat.Constituency

object MapStore {

  val getConstituencies = new SelectList[Constituency] {

    resultMap = new ResultMap[Constituency] {
      result(property = "id", column = "constituency_id")
      result(property = "title", column = "constituency_title")
    }

    def xsql = <xsql>
    SELECT constituency_id, constituency_title
    FROM stat_bank_constituencies
    </xsql>
  }

  val getElectoralDivisions = new SelectListBy[Long, CountyElectoralDivision] {

    resultMap = new ResultMap[CountyElectoralDivision] {
        result(property = "id", column = "gid")
        result(property = "label", column = "saps_label")
        result(property = "county", column = "county")
        result(property = "geometry", column = "geom")
    }

    def xsql = <xsql>
      SELECT ed.gid, ed.county, ed.saps_label,
      ST_ASGEOJSON(ST_TRANSFORM(ST_SETSRID(ST_SIMPLIFY(ed.geom, 100), 29902), 4326)) as geom
      FROM electoral_divisions ed, counties c
      WHERE c.county_id = #{{id}}
      AND c.county = ed.county
      </xsql>
  }

  val getElectoralDivision = new SelectListBy[Long, String] {

    def xsql = <xsql>
      SELECT ST_ASGEOJSON(
        ST_TRANSFORM(ST_SETSRID(geom, 29902), 4326)) as geom
      FROM electoral_divisions
      WHERE gid = #{{id}}
    </xsql>
  }

  val getCountyBounds = new SelectList[CountyGeom] {

    resultMap = new ResultMap[CountyGeom] {
      result(property = "id", column = "county_id")
      result(property = "name", column = "county")
      result(property = "geom", column = "geom")
    }
    def xsql =
      """
        SELECT ST_asGeoJson(
          ST_Transform(ST_SETSRID(ST_SIMPLIFY(cb.geom, 80), 29902), 4326)) as geom,
        c.county_id, c.county
        FROM counties as c, county_boundries as cb
        WHERE c.county = cb.countyname
      """
  }

  def bind = Seq(getConstituencies, getElectoralDivisions, getElectoralDivision, getCountyBounds)
}