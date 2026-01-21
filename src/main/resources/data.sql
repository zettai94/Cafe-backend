--Remove existing data and restart primary key sequences--
--while maintaining table structure--
--should be deleted when moving to production (for data persistence)--
TRUNCATE TABLE product, inventory RESTART IDENTITY CASCADE;

INSERT INTO product (product_name, product_category, product_price, descrip, product_image_url)
VALUES ('Biscoff Tiramisu', 'CAKE', 5.50, 'Classic tiramisu with a twist','../images/biscoffTiramisu.png'), 
        ('Cranberry Scones', 'PASTRY', 3.00, 'Flaky & buttery, cranberries & hint of orange','../images/scones.png'),
        ('Kyoto''s Summer', 'CAKE', 5.25, 'Creamy & rich matcha cake with adzuki bean, & mochi base', '../images/kyotosSummer.png'),
        ('Hot Chocolate', 'BEVERAGE', 4.75, 'Rich & creamy hot chocolate with silky microfoam milk', '../images/hotChoco.png'),
        ('Latte', 'BEVERAGE', 4.25, 'Rich espresso with steamed milk', '../images/coffee.jpg'),
        ('Flat White', 'BEVERAGE', 4.25, 'Australian-style rich espresso with thin silky microfoam', '../images/flatWhite.jpg')
ON CONFLICT (product_name) DO NOTHING;

INSERT INTO inventory (product_id, current_qty)
VALUES ((SELECT product_id FROM product WHERE product_name = 'Biscoff Tiramisu'), 10), --Biscoff Tiramisu--
        ((SELECT product_id FROM product WHERE product_name = 'Cranberry Scones'), 5), --Scones; expecting warning sign for low stock--
        ((SELECT product_id FROM product WHERE product_name = 'Kyoto''s Summer'), 0)  --Kyoto's Summer; expecting it to be sold out--
 ON CONFLICT (product_id) DO NOTHING;