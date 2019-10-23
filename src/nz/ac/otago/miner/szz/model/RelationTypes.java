package nz.ac.otago.miner.szz.model;

import org.neo4j.graphdb.RelationshipType;

public enum RelationTypes implements RelationshipType {
	EVOLVES_TO,
	ORIGINATED_FROM
}
