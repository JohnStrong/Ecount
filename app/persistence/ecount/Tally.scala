package persistence.ecount

import org.mybatis.scala.mapping._

import models.ecount.tallysys._
import models.ecount.stat.{ElectionCandidate, Election}

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

  val insertNewAccount = new Insert[RepresentativeAccount] {
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

  val getElectionById = new SelectOneBy[Int, Election] {
    resultMap = new ResultMap[Election] {
      result(property = "id", column = "election_id")
      result(property = "title", column = "election_title")
    }

    def xsql = <xsql>
      SELECT e.election_id, e.election_title
      FROM stat_bank_elections as e,
      tally_sys_ballot_box as b
      WHERE b.ballot_box_id = #{{id}}
      AND e.election_id = b.election_id
    </xsql>
  }

  val getElectionCandidates = new SelectListBy[Int, ElectionCandidate] {

    resultMap = new ResultMap[ElectionCandidate] {
      result(property = "id", column = "candidate_id")
      result(property = "name", column = "candidate_name")
    }

    def xsql = <xsql>
      SELECT cc.candidate_id, cc.candidate_name
      FROM
      stat_bank_tally_constituency_candidates as cc,
      stat_bank_election_to_constituency as ec
      WHERE ec.election_id = #{{id}}
      AND cc.constituency_id = ec.constituency_id
    </xsql>
  }

  val getAccountByUsername = new SelectOneBy[String, RepresentativeAccount] {
    resultMap = new ResultMap[RepresentativeAccount] {
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

  def bind = Seq(
    getElectionById,
    getElectionCandidates,
    getInactiveBallotBoxes,
    insertNewAccount,
    isUnique,
    isValidVerificationKey,
    lockBallotBox,
    getAccountByUsername)
}
