# TODO 

Create pedagogical samples of creating and using a wide array of UI components
available in the JB platform.

Samples in the `ui` package: 

- [x] Notification
- [x] Dialog wrapper
- [ ] Popup
- [ ] Kotlin UI DSL
- [ ] Settings panel (that can save / load state)
- [ ] File and class chooser
- [ ] Editor component
- [ ] List and tree controls
- [ ] Tool window

# Running the project

## LogService and PersistentStateComponent

To find the IDEA log look at the `$PROJECT_DIR/build/idea-sandbox/system/log
/idea.log` file. A simple command to do this (from the project directory) is:

```shell script
find . -name "idea.log" | xargs tail -f | grep MyPlugin
```

To find the `"logServiceData.xml"` take a look at the `$PROJECT_DIR/build
/idea-sandbox/config/options/logServiceData.xml` file. A simple command to do
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