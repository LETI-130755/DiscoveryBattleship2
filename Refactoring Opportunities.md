| local | nome do cheiro no código | nome da refabricação | número de alun@ |
| :--- | :--- | :--- | :--- |
| `Game::fireSingleShot` | Duplicate expression (`!ship.stillFloating()`) | Introduce Variable | 129808 |
| `Game::printBoard` | Long method / Print legend responsibility | Extract Method | 129808 |
| `Game::printLegend` | Magic literal / Hardcoded legend header string | Introduce Constant | 129808 |
| `Game::fireSingleShot` | Unclear variable name (`b`) | Rename | 129808 |
| `Game::fireSingleShot` | Temporary local variable (`ship`) | Inline Variable | 129808 |
| `Game::printLegend` | Fixed legend header dependency / Hardcoded argument | Introduce Parameter | 129808 |