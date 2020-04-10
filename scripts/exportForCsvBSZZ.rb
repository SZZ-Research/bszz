require 'csv'
require 'pg'
require 'json'

project_name = ARGV[0]

#bfc equivale ao fix revision
#bic equivale ao revision
#{{{ persist_bugfixes(project_csv)
def persist_bugfixes(project_name)
    p 'entering the persist_bugfixes() method'

    # file = File.read(project_json)
    # data = JSON.parse(file)

  begin
    con = PG.connect :dbname => 'bszz', :user => 'postgres', :password => 'postgres'
    p 'connection established'

    # count = 0;
    # size = data.size()

    # p "inserting issuecode: #{issuecode}, revisionnumber: #{revisionnumber}, commitdate: #{commitdate}, issuetype: #{issuetype}, project_name: #{project_name}"
    query = "SELECT revision, fixrevision, project FROM bszzbic  WHERE project = '#{project_name}';"
    responseQuery = con.exec(query)
    count = 0
    size = responseQuery.count
    CSV.open("bszz_out.csv", "w") do |csv|
      csv << ["bfc", "bic", "name"]
      responseQuery.each do | a |
        count += 1
        print(a["project"], " bic = ", a["revision"], " bfc = ", a["fixrevision"], " -- Line #{count} of #{size}\n")
        csv << [a["fixrevision"], a["revision"], a["project"]]
      end
    end
    p "Finish!"
  rescue PG::Error => e
    p e.message
  ensure
    con.close
  end
end
#}}}

persist_bugfixes(project_name)
