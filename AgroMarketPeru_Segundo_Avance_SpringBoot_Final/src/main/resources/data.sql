INSERT IGNORE INTO productos(id,nombre,categoria,tienda,precio,imagen,url) VALUES
(1,'Arroz Costeño Saco 5kg','Abarrotes','Plaza Vea',21.50,'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=700&q=80','https://www.plazavea.com.pe/'),
(2,'Arroz Costeño Saco 5kg','Abarrotes','Tottus',22.90,'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=700&q=80','https://www.tottus.com.pe/'),
(3,'Aceite Primor 1L','Abarrotes','Tottus',9.70,'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=700&q=80','https://www.tottus.com.pe/'),
(4,'Aceite Primor 1L','Abarrotes','Wong',10.40,'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=700&q=80','https://www.wong.pe/'),
(5,'Leche Gloria Six Pack','Lácteos','Metro',23.50,'https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=700&q=80','https://www.metro.pe/'),
(6,'Leche Gloria Six Pack','Lácteos','Plaza Vea',24.20,'https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=700&q=80','https://www.plazavea.com.pe/'),
(7,'Papa Blanca 1kg','Frutas y Verduras','Metro',4.90,'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=700&q=80','https://www.metro.pe/'),
(8,'Plátano de Seda 1kg','Frutas y Verduras','Wong',6.20,'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=700&q=80','https://www.wong.pe/');
INSERT IGNORE INTO historial_precios(producto_id,precio,registrado_en) VALUES
(1,24.90,'2026-06-30 12:00:00'),(1,23.50,'2026-07-31 12:00:00'),(1,22.40,'2026-08-31 12:00:00'),(1,21.50,'2026-09-30 12:00:00'),
(2,25.20,'2026-06-30 12:00:00'),(2,24.40,'2026-07-31 12:00:00'),(2,23.80,'2026-08-31 12:00:00'),(2,22.90,'2026-09-30 12:00:00'),
(3,11.20,'2026-06-30 12:00:00'),(3,10.80,'2026-07-31 12:00:00'),(3,10.20,'2026-08-31 12:00:00'),(3,9.70,'2026-09-30 12:00:00'),
(4,11.40,'2026-06-30 12:00:00'),(4,11.10,'2026-07-31 12:00:00'),(4,10.70,'2026-08-31 12:00:00'),(4,10.40,'2026-09-30 12:00:00'),
(5,26.90,'2026-06-30 12:00:00'),(5,25.80,'2026-07-31 12:00:00'),(5,24.70,'2026-08-31 12:00:00'),(5,23.50,'2026-09-30 12:00:00'),
(6,27.10,'2026-06-30 12:00:00'),(6,26.00,'2026-07-31 12:00:00'),(6,25.10,'2026-08-31 12:00:00'),(6,24.20,'2026-09-30 12:00:00'),
(7,5.40,'2026-06-30 12:00:00'),(7,5.20,'2026-07-31 12:00:00'),(7,5.00,'2026-08-31 12:00:00'),(7,4.90,'2026-09-30 12:00:00'),
(8,6.80,'2026-06-30 12:00:00'),(8,6.60,'2026-07-31 12:00:00'),(8,6.40,'2026-08-31 12:00:00'),(8,6.20,'2026-09-30 12:00:00');
