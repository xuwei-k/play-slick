package models

import play.api.libs.json.{Json, OWrites}

/** Row of data from the table "records" */
case class Record(id: Int, name: String)

object Record {
  implicit val recordWrites: OWrites[Record] = Json.writes[Record]
}