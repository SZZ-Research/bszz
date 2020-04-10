--
-- PostgreSQL database dump
--

-- Dumped from database version 10.12
-- Dumped by pg_dump version 10.12

-- Started on 2020-04-08 14:11:21

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12924)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 3216 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- TOC entry 286 (class 1255 OID 538768)
-- Name: _final_median(numeric[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public._final_median(numeric[]) RETURNS numeric
    LANGUAGE sql IMMUTABLE
    AS $_$
   SELECT AVG(val)
   FROM (
     SELECT val
     FROM unnest($1) val
     ORDER BY 1
     LIMIT  2 - MOD(array_upper($1, 1), 2)
     OFFSET CEIL(array_upper($1, 1) / 2.0) - 1
   ) sub;
$_$;


ALTER FUNCTION public._final_median(numeric[]) OWNER TO postgres;

--
-- TOC entry 972 (class 1255 OID 538769)
-- Name: median(numeric); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE public.median(numeric) (
    SFUNC = array_append,
    STYPE = numeric[],
    INITCOND = '{}',
    FINALFUNC = public._final_median
);


ALTER AGGREGATE public.median(numeric) OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;



--
-- TOC entry 270 (class 1259 OID 539110)
-- Name: linkedissuessvn; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.linkedissuessvn (
    svn_entry_id integer DEFAULT 0 NOT NULL,
    issuecode character varying,
    revisionnumber character varying,
    author character varying,
    commitdate character varying,
    commitmessage character varying,
    issueid integer DEFAULT 0 NOT NULL,
    projectname character varying,
    issueurl character varying,
    issuetype character varying,
    issuepriority character varying,
    status character varying(255) DEFAULT NULL::character varying,
    resolution character varying(255) DEFAULT NULL::character varying,
    assignee character varying(255) DEFAULT NULL::character varying,
    reporter character varying(255) DEFAULT NULL::character varying,
    title character varying,
    summary character varying,
    description character varying,
    environment character varying,
    labels character varying,
    createddate character varying(255) DEFAULT NULL::character varying,
    updateddate character varying(255) DEFAULT NULL::character varying,
    resolveddate character varying(255) DEFAULT NULL::character varying,
    datasetversion character varying(255) DEFAULT NULL::character varying,
    fixversion character varying(255) DEFAULT NULL::character varying,
    due character varying(255) DEFAULT NULL::character varying,
    votes character varying(255) DEFAULT NULL::character varying,
    watches character varying(255) DEFAULT NULL::character varying,
    keyid character varying(300) DEFAULT NULL::character varying,
    keyvalue character varying(300) DEFAULT NULL::character varying,
    timeoriginalestimate character varying(500) DEFAULT NULL::character varying,
    timeestimate character varying(500) DEFAULT NULL::character varying,
    component character varying,
    buildinfoversion character varying(100) DEFAULT NULL::character varying,
    buildnumber character varying(150) DEFAULT NULL::character varying,
    builddate character varying(150) DEFAULT NULL::character varying,
    issample boolean
);


ALTER TABLE public.linkedissuessvn OWNER TO postgres;


--
-- TOC entry 283 (class 1259 OID 539190)
-- Name: szz_project_lastrevisionprocessed; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.szz_project_lastrevisionprocessed (
    project character varying,
    lastrevisionprocessed character varying
);


ALTER TABLE public.szz_project_lastrevisionprocessed OWNER TO postgres;


--
-- TOC entry 202 (class 1259 OID 538797)
-- Name: bicbszz; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bszzbic (
     linenumber bigint,
    path character varying,
    content character varying,
    project character varying,
    szz_date timestamp without time zone,
    copypath character varying,
    copyrevision bigint,
    mergerev boolean,
    branchrev boolean,
    changeproperty boolean,
    missed boolean,
    furtherback boolean,
    contentdiff character varying,
    issample boolean,
    diffjmessage character varying,
    diffjlocation character varying,
    refdiffindex integer,
    adjustmentindex integer,
    indexposrefac integer,
    indexfurtherback integer,
    indexchangepath integer,
    isrefac boolean,
    revision character varying,
    fixrevision character varying,
    islargest boolean,
    islatest boolean,
    startrevision character varying,
    startpath character varying,
    startlinenumber bigint,
    startcontent character varying,
    isvalidfix boolean,
    hasmatchmaszz boolean,
    isvalidresult boolean
);


ALTER TABLE public.bszzbic OWNER TO postgres;



