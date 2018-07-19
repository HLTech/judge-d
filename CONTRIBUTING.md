# Contributing

## Code formatting

Stick to present standards and please, try to avoid reformatting existing code.

## Coding tips

* Before coding please comment on issue that you are going to be working on.
* Before coding make sure you know what has to be done. Ask as much as needed to fully understand flow. Clarify everything 
you are not sure about.

## Coding guidelines

* Avoid comments. Use them where they are *really* necessary. Self-explanatory names and clean code over comments.
* Always provide tests to your change.
* Avoid additional dependencies just to have one utility method. Add only necessary dependenceis.

## Testing

* [Spock](http://spockframework.org/) as a base testing framework.

## Database versioning

* Never change existing changesets. Create new ones.
* In case of renaming (table/column) make sure that you've renamed all corresponding constraints like foreign keys or sequences.

## Commits

* Add corresponding issue id to commit message.
* Use self explanatory messages for commits.
