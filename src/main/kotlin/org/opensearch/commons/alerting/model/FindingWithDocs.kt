package org.opensearch.commons.alerting.model

import org.apache.logging.log4j.LogManager
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.BaseModel
import java.io.IOException

private val log = LogManager.getLogger(Finding::class.java)

class FindingWithDocs : BaseModel {
    var finding: Finding
    var documents: List<FindingDocument>

    constructor(
        finding: Finding,
        documents: List<FindingDocument>
    ) : super() {
        this.finding = finding
        this.documents = documents
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        finding = Finding.readFrom(sin),
        documents = sin.readList((FindingDocument)::readFrom)
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        finding.writeTo(out)
        out.writeCollection(documents)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .startObject("finding_with_docs")
            .field(FINDING_FIELD, finding)
            .field(DOCUMENTS_FIELD, documents.toTypedArray())
            .endObject()
        builder.endObject()
        return builder
    }

    companion object {
        const val FINDING_FIELD = "finding"
        const val DOCUMENTS_FIELD = "document_list"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): FindingWithDocs {
            lateinit var finding: Finding
            val documents: MutableList<FindingDocument> = mutableListOf()

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    FINDING_FIELD -> finding = Finding.parse(xcp)
                    DOCUMENTS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            documents.add(FindingDocument.parse(xcp))
                        }
                    }
                }
            }

            return FindingWithDocs(
                finding = finding,
                documents = documents
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): FindingWithDocs {
            return FindingWithDocs(sin)
        }
    }
}
