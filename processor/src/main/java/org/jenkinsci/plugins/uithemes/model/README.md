# UI Theme Model

A UI Theme is an abstraction that encapsulates a set of core/plugin defined resources
(styles for now, but in time may include Javascript) that represent different implementations
of a styling theme, only one of which is applied at runtime.

An example of a UI Theme might be an "icon" theme, for which you would have a "classic" (default)
implementation to represent the standard Jenkins icons. On top of that you could could have other
icon theme implementations e.g. a "font-awesome" implementation.

The user would be allowed to select one implementation of any UI Theme.
