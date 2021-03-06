package persistence.ecount

import org.mybatis.scala.mapping._

import service.dispatch.tallysys.{UpdatedCandidateResult, NewCandidateResult}

import models.tallysys._
import models.stat.{ElectionCandidate, Election}

import service.dispatch.tallysys.{PartialTally, CandidateExtractor}

object Tally {

  val getInactiveBallotBoxes = new SelectListBy[String, ElectionBallotBox] {
    resultMap = new ResultMap[ElectionBallotBox] {
      result(property = "id", column = "ballot_box_id")
      result(property = "title", column = "ballot_title")
    }

    def xsql = <xsql>
      SELECT b.ballot_box_id, b.ballot_title
      FROM tally_sys_ballot_box as b, tally_sys_verification as v
      WHERE v.verification_key = #{{key}}
      AND b.verification_id = v.verification_id
      AND b.active = 'false'
    </xsql>
  }

  val isValidVerificationKey = new SelectOneBy[String, String] {
    def xsql = <xsql>
      SELECT verification_key
      FROM tally_sys_verification
      WHERE verification_key = #{{key}}
    </xsql>
  }

  val isUnique = new SelectOneBy[String, String] {
    def xsql = <xsql>
    SELECT username
    FROM tally_sys_user_access
    WHERE username = #{{username}}
    </xsql>
  }

  val insertNewAccount = new Insert[NewRepresentativeAccount] {
    def xsql = <xsql>
     INSERT INTO tally_sys_user_access(username, ballot_box_id, salt, hash, fname, surname)
     VALUES (#{{username}}, #{{ballotBoxId}}, #{{salt}}, #{{hash}}, #{{fname}}, #{{surname}})
    </xsql>
  }

  val lockBallotBox = new Update[Int] {
    def xsql = <xsql>
      UPDATE tally_sys_ballot_box
      SET active = 'true'
      WHERE ballot_box_id = #{{id}}
    </xsql>
  }

  val getElectionById = new SelectOneBy[Int, TallyElection] {
    resultMap = new ResultMap[TallyElection] {
      result(property = "id", column = "election_id")
      result(property = "title", column = "election_title")
      result(property = "constituencyId", column = "constituency_id")
    }

    def xsql = <xsql>
      SELECT e.election_id, e.election_title, constituency_id
      FROM stat_bank_elections as e,
      tally_sys_ballot_box as b
      WHERE b.ballot_box_id = #{{id}}
      AND e.election_id = b.election_id
    </xsql>
  }

  val getElectionCandidates = new SelectListBy[CandidateExtractor, ElectionCandidate] {

    resultMap = new ResultMap[ElectionCandidate] {
      result(property = "id", column = "candidate_id")
      result(property = "name", column = "candidate_name")
      result(property = "party", column = "party_name")
    }

    def xsql = <xsql>
      SELECT cc.candidate_id, cc.candidate_name, p.party_name
      FROM
      stat_bank_tally_constituency_candidates as cc,
      stat_bank_election_to_constituency as ec,
      stat_bank_parties as p
      WHERE ec.election_id = #{{electionId}}
      AND cc.constituency_id = #{{constituencyId}}
      AND cc.constituency_id = ec.constituency_id
      AND cc.party_id = p.party_id
    </xsql>
  }

  val getAccountByUsername = new SelectOneBy[String, NewRepresentativeAccount] {
    resultMap = new ResultMap[NewRepresentativeAccount] {
      result(property = "username", column = "username")
      result(property = "fname", column = "fname")
      result(property = "surname", column = "surname")
      result(property = "salt", column = "salt")
      result(property = "hash", column = "hash")
      result(property = "ballotBoxId", column = "ballot_box_id")
    }

    def xsql = <xsql>
      SELECT username, fname, surname, salt, hash, ballot_box_id
      FROM tally_sys_user_access
      WHERE username = #{{id}}
    </xsql>
  }

  val deleteAccountByUsername = new Delete[String] {
    def xsql = <xsql>
      DELETE FROM tally_sys_user_access
      WHERE username = #{{username}}
    </xsql>
  }

  val getBallotBoxElectionDetails = new SelectOneBy[Int, ElectionBallotBox] {
    resultMap = new ResultMap[ElectionBallotBox] {
      result(property = "id", column = "ballot_box_id")
      result(property = "dedId", column = "ded_id")
      result(property = "electionId", column = "election_id")
      result(property = "constituencyId", column = "constituency_id")
    }

    def xsql = <xsql>
      SELECT ballot_box_id, ded_id, election_id, constituency_id
      FROM tally_sys_ballot_box
      WHERE ballot_box_id = #{{id}}
    </xsql>
  }

  val hasPartialTallyForCandidate = new SelectOneBy[PartialTally, Int] {
    def xsql = <xsql>
      SELECT cr.results_id
      FROM stat_bank_tally_candidate_to_results as ctr,
      stat_bank_tally_candidate_results as cr
      WHERE ctr.candidate_id = #{{candidateId}}
      AND cr.ded_id = #{{dedId}}
      AND cr.results_id = ctr.results_id
    </xsql>
  }

  val insertNewCandidateTallyResult = new Insert[NewCandidateResult] {
    def xsql = <xsql>
      INSERT INTO stat_bank_tally_candidate_results(results_id, count, ded_id)
      VALUES
        (nextval('candidate_results_id_seq'), #{{candidateTally}}, #{{dedId}});
      INSERT INTO stat_bank_tally_candidate_to_results
      VALUES
        ((SELECT MAX(results_id) from stat_bank_tally_candidate_results), #{{candidateId}});
      UPDATE stat_bank_elections
      SET available = 'true' WHERE election_id = #{{electionId}};
    </xsql>
  }

  val updateCandidateTallyResult = new Update[UpdatedCandidateResult] {
    def xsql = <xsql>
      UPDATE stat_bank_tally_candidate_results
      SET count = count + #{{tally}}
      WHERE results_id = #{{resultsId}}
    </xsql>
  }

  val getCountyIdForConstituency = new SelectOneBy[Int, Int] {
    def xsql = <xsql>
      SELECT county_id
      FROM stat_bank_counties_constituencies
      WHERE constituency_id = #{{id}}
    </xsql>
  }

  def bind = Seq(
    getElectionById,
    getElectionCandidates,
    getInactiveBallotBoxes,
    insertNewAccount,
    isUnique,
    isValidVerificationKey,
    lockBallotBox,
    getAccountByUsername,
    deleteAccountByUsername,
    getBallotBoxElectionDetails,
    hasPartialTallyForCandidate,
    insertNewCandidateTallyResult,
    updateCandidateTallyResult,
    getCountyIdForConstituency
  )
}
