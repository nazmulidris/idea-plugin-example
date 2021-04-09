# Learn more about IDEA Plugin Development

This repo is a pedagogical example of an IDEA plugin. To learn more about plugins please read the
tutorial that accompanies this code example on developerlife.com -
[Introduction to creating IntelliJ IDEA plugins](http://localhost:4000/2020/11/20/idea-plugin-example-intro/).

# Running the project

## LogService and PersistentStateComponent

To find the IDEA log look at the `$PROJECT_DIR/build/idea-sandbox/system/log /idea.log` file. A
simple command to do this (from the project directory) is:

```shell script
find . -name "idea.log" | xargs tail -f | grep MyPlugin
```

To find the `"logServiceData.xml"` take a look at the
`$PROJECT_DIR/build /idea-sandbox/config/options/logServiceData.xml` file. A simple command to do
this (from the project directory) is:

```shell script
find . -name "logServiceData.xml" | xargs subl -n
```

Also, to delete this file, if it gets too big:

```shell script
find . -name "logServiceData.xml" | xargs rm
```

# References

Getting started w/ writing your first plugin

- [Writing your first plugin](https://tinyurl.com/y67ygovg)
- [Organizing and using icons in plugins](https://tinyurl.com/y33rbxst)
- [github issues thread on IDEA icons](https://tinyurl.com/yxe8yhxt)
- [List of default icons in IDEA, AllIcons.java](https://tinyurl.com/y4nh4nwu)
- [IDEA plugin services and components](https://tinyurl.com/y4n4l4wd)
  - [github repo for plugin component & service](https://tinyurl.com/y6o9dlhb)
- [Details about actions](https://tinyurl.com/yxaoflp6)
- [JetBrains official code samples for plugins](https://tinyurl.com/y69ufr68)

Extension Points and extensions

- [IDEA docs on extensions and extension points](https://tinyurl.com/y6a4xafo)

Services

- [IDEA docs on services](https://tinyurl.com/yy9tsyq7)

Notifications

- [IDEA docs on Notifications](https://tinyurl.com/yxkvn4ad)
- [Code samples](https://tinyurl.com/y45xww6m)
- [Code samples](https://tinyurl.com/y4zd6t5q)

Logging

- [How to log in IDEA](https://tinyurl.com/y2bll4ph)

Tutorials on writing plugins

- [Simple custom dictionary loader plugin](https://tinyurl.com/y2n8ymsh)
  - [github repo for this](https://tinyurl.com/y3c4tmyu)
- [Simple stackoverflow lookup plugin](https://tinyurl.com/y336wul6)
  - [github repo for this](https://tinyurl.com/y5xwytfj)

Icons

- [Material Design Icons](https://tinyurl.com/y4op6mnt)
- [SVG resizing](https://tinyurl.com/y6mzgofw)

Git

- [Rebase feature branch to master](https://tinyurl.com/md6v2oc)

# Change master to main (2020-07-16)

The
[Internet Engineering Task Force (IETF) points out](https://tools.ietf.org/id/draft-knodel-terminology-00.html#rfc.section.1.1.1)
that "Master-slave is an oppressive metaphor that will and should never become fully detached from
history" as well as "In addition to being inappropriate and arcane, the
[master-slave metaphor](https://github.com/bitkeeper-scm/bitkeeper/blob/master/doc/HOWTO.ask?WT.mc_id=-blog-scottha#L231-L232)
is both technically and historically inaccurate." There's lots of more accurate options depending on
context and it costs me nothing to change my vocabulary, especially if it is one less little speed
bump to getting a new person excited about tech.

You might say, "I'm all for not using master in master-slave technical relationships, but this is
clearly an instance of master-copy, not master-slave"
[but that may not be the case](https://mail.gnome.org/archives/desktop-devel-list/2019-May/msg00066.html).
Turns out the original usage of master in Git very likely came from another version control system
(BitKeeper) that explicitly had a notion of slave branches.

- https://dev.to/lukeocodes/change-git-s-default-branch-from-master-19le
- https://www.hanselman.com/blog/EasilyRenameYourGitDefaultBranchFromMasterToMain.aspx

[#blacklivesmatter](https://blacklivesmatter.com/)
