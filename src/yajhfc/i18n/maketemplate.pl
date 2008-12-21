#!/usr/bin/perl -w
# Simple filter to create a template from CommandLineOptions.po

my $numskip = 0;
my $msgstr_mode = 0; # 0: remove, 1: keep, 2: remove quoted stuff

while (defined(my $line = <>)) {
	if ($numskip > 0) {
		$numskip--;
	} else {
		if ($line =~ /msgid ""/) {
			$msgstr_mode = 1;
			print "$line";
		} elsif ($line =~ /msgid/) {
			$msgstr_mode = 0;
			print "$line";
		} elsif ($msgstr_mode == 0 && $line =~ /msgstr .*/) {
			print "msgstr \"\"\n";
			$msgstr_mode = 2;
		} elsif ($msgstr_mode == 2 && $line =~ /\s*\".*\"/) {
			# Do nothing
		} elsif ($line =~ m/# *DO NOT translate/i) {
			$numskip = 3; # Skip the next 3 lines
		} else {
			print "$line";
		}
	}
}

