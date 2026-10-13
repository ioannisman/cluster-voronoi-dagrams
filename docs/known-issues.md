# Known issues

Small limitations and rough edges.

- Add cluster does not place the new cluster near the mouse
- Zooming out far enough that a k-ellipse distance grid would exceed its size limit hides the diagram, skeleton, and region subdivision until you zoom back in. The warning dialog appears once for that period; the canvas says to zoom in. Pan, zoom, and handle editing stay available.
- Dragging a k-ellipse rebuilds that member's distance field before the diagram can update. The other members keep their fields. A fast drag preview only draws fewer pixels; it does not switch to a coarser distance method.
- A k-ellipse feature thinner than the grid spacing (2 world units) may not appear as an interior in the distance field. Zooming in does not refine the grid.
- Cursor toggles (pointer icons left of gadgets) choose who receives canvas input. Selecting a gadget’s toggle steals input from the drawing so handle editing stops; switch back with `~` or the drawing’s cursor toggle.
