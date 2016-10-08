import sys, re

# Ad-hoc script to rip values out of Garmin Connect XML files (*.gpx)
# using regex rather than an xml parser due to time constraints.
#
# @author Sam Hunt
#
# Usage:
#   python parse_xml_gps_data.py <gps-xml-filepath> [trapline-number]
#
# e.g.
#   python .\parse_xml_gps_data.py .\activity_1302597875.gpx 12
#
# trapline-number defaults to 1.
#
# bait id and trap type id will be removed at a later date as they 
# will pull defaults from the trapline in future.

input_path = sys.argv[1]
trapline_id = sys.argv[2] if sys.argv[2] else 1

coords_regex = "lat=\"(-?[0-9.]+)\"\\slon=\"(-?[0-9.]+)\""
timestamp_regex = "([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3})"

trapx, trapy, trapts, trapnumber = None, None, None, 1

control, writeline = True, False

with open ("./traps_from_xml.sql", "w") as f_out:
    f_out.write("INSERT INTO public.trap\n")
    f_out.write("    (trap_traplineid, trap_number, trap_coordx, trap_coordy, trap_createdtimestamp, trap_lastresettimestamp, trap_traptypeid, trap_baitid)\n")
    f_out.write("VALUES\n")

    with open(input_path, "r") as f_in:
        for line in f_in:
            if writeline == True:
                f_out.write("    (" + str(trapline_id) + ", " + str(trapnumber) + ", " + trapx + ", " + trapy + ", '" + trapts + "', '" + trapts + "', 1, 1),\n")
                trapx, trapy, trapts, writeline, trapnumber = None, None, None, False, trapnumber+1
            if control == True:
                # look for coords
                m = re.findall(coords_regex, line)
                if m != []:
                    print(m)
                    trapy, trapx, control = m[0][0], m[0][1], False
            else:
                # look for timestamp
                m = re.findall(timestamp_regex, line)
                if m != []:
                    trapts = str(m[0])
                    writeline, control = True, True
print()
print("Check there is a semicolon (not a comma) at EOF.")
