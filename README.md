# wapy-backend

This backend runs on the AWS EC2 and will serve the front with the responses from the controllers.

There are 3 controllers for the web app: Dashboard, Product and Box.

The Dashboard controller shows the user/manager with data from all cameras and stores, about all the products he owns.
The Product controller manage and show statistics about the products.
The Box controller shows a Heat map of the camera field of view and will present statistics about the products that
are in the window the camera is recording.