package persistence.ecount

import org.mybatis.scala.mapping._

import models.ecount.stat._

  object StatStore {

  val getElectionEntries = new SelectList[Election]{

    resultMap = new ResultMap[Election] {
      result(property = "id", column = "election_id")
      result(property = "title", column = "election_title")
      result(property = "tallyDate", column = "tally_date")
    }

    def xsql = <xsql>
      SELECT election_id, election_title, tally_date
      FROM stat_bank_elections
      ORDER BY tally_date DESC
    </xsql>
  }

  val getCountyConstituencies = new SelectListBy[Long, Constituency] {

    resultMap = new ResultMap[Constituency] {
      result( property = "id", column = "constituency_id")
      result( property = "title", column = "constituency_title")
    }

    def xsql = <xsql>
      SELECT sbc.constituency_id, sbc.constituency_title
      FROM
      stat_bank_constituencies as sbc,
      stat_bank_counties_constituencies sbcc, counties as c
      WHERE c.county_id = #{{id}}
      AND sbcc.county_id = c.county_id
      AND sbc.constituency_id = sbcc.constituency_id;
    </xsql>
  }

  val getConstituencyElectionCandidates = new SelectListBy[TallyResultsExtractor, ElectionCandidate] {

    resultMap = new ResultMap[ElectionCandidate] {
      result(property = "id", column = "candidate_id")
      result(property = "name", column = "candidate_name")
    }

    def xsql = <xsql>
      SELECT ca.candidate_id, ca.candidate_name
      FROM stat_bank_constituencies as c,
      stat_bank_tally_election_to_const_candidates as ec,
      stat_bank_tally_constituency_candidates as ca
      WHERE c.constituency_id = #{{constituencyId}}
      AND ec.election_id = #{{electionId}}
      AND ca.candidate_id = ec.candidate_id
      AND ca.constituency_id = c.constituency_id;
    </xsql>
  }

  val getConstituencyTallyResults = new SelectListBy[Long, ElectionCandidateTally] {

    resultMap = new ResultMap[ElectionCandidateTally] {
      result(property = "result", column = "count")
      result(property = "dedId", column = "ded_id")
    }

    def xsql = <xsql>
      SELECT cr.count, cr.ded_id
      FROM stat_bank_tally_candidate_to_results as ctr,
      stat_bank_tally_candidate_results as cr
      WHERE ctr.candidate_id = #{{id}}
      AND cr.results_id = ctr.results_id
    </xsql>
  }

  def bind = Seq(
    getElectionEntries,
    getCountyConstituencies,
    getConstituencyElectionCandidates,
    getConstituencyTallyResults
  )
}