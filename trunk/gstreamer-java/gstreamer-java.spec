%global arch_with_swt %{ix86} x86_64 ppc ppc64 ia64 sparc sparc64

Summary:	Java interface to the gstreamer framework
Name:		gstreamer-java
Version:	1.3
Release:	0%{?dist}
License:	LGPLv3 and CC-BY-SA
Group:		System Environment/Libraries
URL:		http://code.google.com/p/gstreamer-java/
# The source for this package was pulled from upstream's vcs.  Use the
# following commands to generate the tarball:
#  hg clone -r 541 https://kenai.com/hg/gstreamer-java~mercurial gstreamer-java
#  tar cjvf gstreamer-java-src-1.1.tar.bz2 --exclude .hg* gstreamer-java
#Source:	http://gstreamer-java.googlecode.com/files/%{name}-src-%{version}.tar.bz2
# but now use the released version
Source:		http://gstreamer-java.googlecode.com/files/%{name}-src-%{version}.zip
Patch:		%{name}-%{version}-swt.patch
BuildRoot:	%{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
# for ExcludeArch and no noarch see bug: 468831
# since noarch pacakge can't contain ExcludeArch :-( imho it's an rpm bug
#BuildArch:	noarch
ExcludeArch:	ppc ppc64
# Don't build debuginfo packages since it's actualy a noarch package
%global debug_package %{nil}

Requires:	java >= 1:1.6.0
Requires:	jpackage-utils
Requires:	jna
# versioned gstreamer required since earlier version don't have features like:
# gst_caps_merge, gst_query_new_latency
Requires:	gstreamer >= 0.10.19
Requires:	gstreamer-plugins-base >= 0.10.19
Conflicts:	gstreamer-plugins-good < 0.10.7
Conflicts:	gstreamer-plugins-ugly < 0.10.7
Conflicts:	gstreamer-plugins-bad < 0.10.6

BuildRequires:	java-devel >= 1:1.6.0
BuildRequires:	jpackage-utils
BuildRequires:	jna
%ifarch %{arch_with_swt}
BuildRequires:	libswt3-gtk2
%endif
BuildRequires:	gstreamer-devel >= 0.10.19
BuildRequires:	gstreamer-plugins-base-devel >= 0.10.19
BuildRequires:	gstreamer-plugins-good-devel >= 0.10.7
#BuildRequires:	gstreamer-plugins-ugly-devel >= 0.10.7
#BuildRequires:	gstreamer-plugins-bad-devel >= 0.10.6
BuildRequires:	ant
BuildRequires:	ant-junit
%if 0%{?fedora} >= 9
BuildRequires:	junit4
%endif

%description
An unofficial/alternative set of java bindings for the gstreamer multimedia
framework.

%package javadoc
Summary:	Javadocs for %{name}
Group:		Documentation
Requires:	%{name} = %{version}-%{release}
Requires:	jpackage-utils

%description javadoc
This package contains the API documentation for %{name}.


%prep
%setup -q -n %{name}-src-%{version}
cp -p src/org/freedesktop/tango/COPYING COPYING.CC-BY-SA
# remove prebuild binaries
find . -name '*.jar' -exec rm {} \;

# replace included jar files with the system packaged version (JNA, SWT, GStreamer plugins dir)
sed -i -e "s,\(file.reference.jna.jar=\).*,\1$(build-classpath jna)," \
	-e "s,\(run.jvmargs=-Djna.library.path=\).*,\1%{_libdir}:$(pkg-config --variable=pluginsdir gstreamer-0.10)," \
	nbproject/project.properties

%ifarch %{arch_with_swt}
sed -i -e "s,\(file.reference.swt.jar=\).*,\1$(find %{_libdir} -name swt*.jar 2>/dev/null|sort|head -1)," \
	nbproject/project.properties
%else
%patch -p1
rm -rf src/org/gstreamer/swt src/org/gstreamer/example/SWTOverlayPlayer.java
%endif


%build
# from Fedora-9 we've got ant-1.7.0 and junit4 while on older releases and EPEL
# have only ant-1.6.5 and junit-3.8.2 therefore on older releases and EPEL we
# have small hacks like ant-1.6.5 need packagenames for javadoc task
# and test targets need ant-1.7.x and junit4 so we skip the test during packaging
%if 0%{?fedora} >= 9
sed -i -e "s,\(file.reference.junit4.jar=\).*,\1$(build-classpath junit4)," \
	nbproject/project.properties
ant
%else
sed -i -e 's,\(<javadoc destdir="${dist.javadoc.dir}" source="${javac.source}"\),\1 packagenames="*",' \
	build.xml
ant jar
ant javadoc
%endif


%install
rm -rf %{buildroot}
mkdir -p -m0755 %{buildroot}%{_javadir}
install -m 0644  dist/*.jar	%{buildroot}%{_javadir}

mkdir -p -m0755 %{buildroot}%{_javadocdir}/%{name}
cp -rp dist/javadoc/* %{buildroot}%{_javadocdir}/%{name}


%clean
rm -rf %{buildroot}


%files
%defattr(-,root,root,-)
%{_javadir}/*
%doc CHANGES COPYING* tutorials/*

%files javadoc
%defattr(-,root,root,-)
%{_javadocdir}/%{name}

%changelog
* Sat Dec 26 2009 Levente Farkas <lfarkas@lfarkas.org> - 1.3-1
- update to version 1.3

* Fri Jul 24 2009 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 1.2-3
- Rebuilt for https://fedoraproject.org/wiki/Fedora_12_Mass_Rebuild

* Fri Jul  3 2009 Levente Farkas <lfarkas@lfarkas.org> - 1.2-2
- don't build debuginfo pacakges since it's actualy a noarch pacakge

* Tue Jun 30 2009 Levente Farkas <lfarkas@lfarkas.org> - 1.2-1
- update to the new upstream version
- don't use build-classpath for SWT on any platform since it's broken in most cases
- add suport for platfrom which has no SWT support

* Tue Feb 24 2009 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 1.0-3
- Rebuilt for https://fedoraproject.org/wiki/Fedora_11_Mass_Rebuild

* Thu Feb 12 2009 Levente Farkas <lfarkas@lfarkas.org> - 1.0-2
- fix spec file to build on x86_64 too

* Tue Nov 11 2008 Levente Farkas <lfarkas@lfarkas.org> - 1.0-1
- update to the new upstream version
- fix EPEL build problems (ant-1.7.0 and junit4)

* Tue Oct 28 2008 Levente Farkas <lfarkas@lfarkas.org> - 0.9-0.3.20081023hg
- add ExcludeArch ppc, ppc64 because of bug 468831

* Sat Oct 25 2008 Levente Farkas <lfarkas@lfarkas.org> - 0.9-0.2.20081023hg
- more spec file cleanup

* Tue Sep  2 2008 Levente Farkas <lfarkas@lfarkas.org> - 0.9
- update to mercurial repo

* Thu Aug  7 2008 Gergo Csontos <gergo.csontos@gmail.com> - 0.9
- remove the manual subpackage and put into the man doc

* Thu Jul 10 2008 Gergo Csontos <gergo.csontos@gmail.com> - 0.8
- Initial release
