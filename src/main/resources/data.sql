INSERT INTO product (product_name, product_category, product_price, descrip, product_image_url)
VALUES ('Biscoff Tiramisu', 'CAKE', 5.50, 'Classic tiramisu with a twist','../images/biscoffTiramisu.png')
ON CONFLICT (product_name) DO NOTHING;

INSERT INTO inventory (product_id, current_qty)
VALUES(1, 10) ON CONFLICT (product_id) DO NOTHING;