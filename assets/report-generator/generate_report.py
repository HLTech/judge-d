from jinja2 import Template
import json
import os
import sys

if len(sys.argv) > 2:
    dredd_response = json.load(open(sys.argv[1]))

    script_directory = os.path.dirname(os.path.realpath(__file__))
    template_file_path = script_directory + "/report_template.html.jinja"
    with open(template_file_path) as template_file:
        template = Template(template_file.read())

    with open(sys.argv[2], 'w') as result:
        try:
            result.write(template.render(
                validationResults = dredd_response["validationResults"]
            ))
        except:
            result.write(template.render(
                errorMessage = dredd_response
            ))

else:
    print('''Script requires two arguments - first is path to validation result json file, second is path to output html file - 
    e.g. py generate_report.py validationResults.json generatedReport.html''')
