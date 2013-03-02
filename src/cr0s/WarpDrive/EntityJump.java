package cr0s.WarpDrive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class EntityJump extends Entity {

    private int moveX;
    private int moveZ;
    public int xCoord;
    public int yCoord;
    public int zCoord;
    public int distance;
    public int dir;
    public int shipLeft;
    public int shipRight;
    public int shipFront;
    public int shipBack;
    public int shipDown;
    public int shipUp;
    public int shipLength;
    public int Xmax;
    public int Zmax;
    public int maxY;
    public int Xmin;
    public int Zmin;
    public int minY;
    public int dx;
    public int dz;
    // Координаты места взрыва при столкновении
    public int blowX, blowY, blowZ;
    // Флаг, обозначающий возможность взрыва
    boolean needToExplode = false;
    // public World worldObj;
    public boolean on = false;
    public JumpBlock ship[];
    public TileEntityReactor reactor;

    boolean isJumping = false;
    int currentIndexInShip = 0;
    
    private final int BLOCKS_PER_TICK = 1000;
    
    private List entityOnShip;
    
    AxisAlignedBB axisalignedbb;
    
    private boolean fromSpace, toSpace;
    
    private EntityPlayer fakePlayer;
    
    public EntityJump(World world) {
        super(world);
    }

    public EntityJump(World world, int x, int y, int z, int _dist, int _direction, int _dx, int _dz, TileEntityReactor parReactor) {
        super(world);

        this.xCoord = x;
        this.posX = (double) x;

        this.yCoord = y;
        this.posY = (double) y;

        this.zCoord = z;
        this.posZ = (double) z;

        this.distance = _dist;
        this.dir = _direction;

        shipLeft = shipRight = shipFront = shipBack = shipDown = shipUp = shipLength = 0;
        this.dx = this.dz = 0;
        this.dx = _dx;
        this.dz = _dz;
        Xmax = Zmax = maxY = Xmin = Zmin = minY = 0;

        // this.worldObj = world;

        System.out.println("[JE] Entity created");

        this.reactor = parReactor;
        
        this.isJumping = false;
    }

    public void killEntity(String reason) {
        if (!reason.isEmpty()) {
            System.out.println("[JUMP] Killed: " + reason);
        }

        worldObj.editingBlocks = false;
        worldObj.setEntityDead(this);
    }

    @SideOnly(Side.SERVER)
    @Override
    public void onUpdate() {
        if (!on || minY < 0 || maxY > 255) {
            killEntity("Entity is disabled or Y-coord error! Cannot jump.");
            return;
        }
               
        // Пропустить тик, ожидая генерации чанков
        //if (!checkForChunksGeneratedIn(getTargetWorld())) {
        //    return;
        //}
        
        if (!isJumping) {
            System.out.println("[JE] Preparing to jump...");
            axisalignedbb = AxisAlignedBB.getBoundingBox(Xmin, minY, Zmin, Xmax, maxY, Zmax);
            this.toSpace   = (dir == -1 && (maxY + distance > 255) && worldObj.provider.dimensionId != WarpDrive.instance.spaceDimID);
            this.fromSpace = (dir == -2 && (minY - distance < 0) && worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID);
        
            prepareToJump();
        } else {
            if (currentIndexInShip >= ship.length-1) {
                isJumping = false;
                finishJump();
            } else { 
                moveEntitys(axisalignedbb, distance, dir, true);                
                moveShip();                   
            }
        }
    }
    
    public World getTargetWorld() {        
        if (toSpace) {
            return DimensionManager.getWorld(WarpDrive.instance.spaceDimID);
        } else if (fromSpace) {
            return DimensionManager.getWorld(0);
        } else {
            return this.worldObj;
        }
    }
    
    public void lockWorlds() {
        getTargetWorld().editingBlocks = true;
        
        // При прыжках между измерениями необходимо блокировать текущий мир
        // Для предотвращения рассыпания проводов на корабле
        if (getTargetWorld().provider.dimensionId != worldObj.provider.dimensionId) {
            worldObj.editingBlocks = true;
        }
    }
    
    public void unlockWorlds() {
        getTargetWorld().editingBlocks = false;
        
        // При прыжках между измерениями необходимо блокировать оба мира
        // Для предотвращения рассыпания проводов на корабле
        if (getTargetWorld().provider.dimensionId != worldObj.provider.dimensionId) {
            worldObj.editingBlocks = false;
        }        
    }
    
    
    /**
     * Установка или удаление блоков под игроками для предотвращения их падения
     * @param removeBlocks удалять блоки
     */
    public void setBlocksUnderPlayers(boolean removeBlocks) {
        List list = this.entityOnShip;

        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof MovingEntity)) {
                    continue;
                }
                
                MovingEntity me = (MovingEntity)obj;
                Entity entity = me.entity;
                
                if (entity instanceof EntityPlayer) {
                    if (!removeBlocks) {
                        worldObj.setBlock(me.oldX, me.oldY - 2, me.oldZ, Block.dirt.blockID);
                    } else
                    {
                        if (worldObj.getBlockId(me.oldX, me.oldY - 2, me.oldZ) == Block.dirt.blockID) {
                            worldObj.setBlock(me.oldX, me.oldY - 2, me.oldZ, 0);
                        }
                    }
                }
            }
        }        
    }
    
    /**
     * Проверка на существование чанков в точке назначения прыжка
     * Если чанки не загружены или не существуют, то происходит их загрузка или генерация
     * @param world Мир, в котором осуществляется проверка
     * @return Результат проверки
     */
    public boolean checkForChunksGeneratedIn(World w) {
        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    final int newX = getNewXCoord(x, 0, z, this.distance, this.dir);
                    final int newZ = getNewZCoord(x, 0, z, this.distance, this.dir);    
                    
                    int chunkX = newX >> 4;
                    int chunkZ = newZ >> 4;
                    
                    if (!w.getChunkProvider().chunkExists(chunkX, chunkZ)) {
                        System.out.println("[JE] Need to generate chunk at (" + chunkX + "; " + chunkZ + "). Creating fake player");
                        w.getChunkProvider().provideChunk(chunkX, chunkZ);
                        
                        return false;
                    }
                }
            }
        }        
        
        return true;
    }
    
    public void messageToAllPlayersOnShip(String msg) {
        List list = this.entityOnShip;

        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof MovingEntity)) {
                    continue;
                }
                
                MovingEntity me = (MovingEntity)obj;
                Entity entity = me.entity;
                
                if (entity instanceof EntityPlayer) {
                    ((EntityPlayer)entity).sendChatToPlayer("[WarpCore] " + msg);
                }
            }
        }
    }
    
    public void prepareToJump() {
        boolean betweenWorlds = false;
        
        // Блокируем мир
        lockWorlds();

        betweenWorlds = fromSpace || toSpace;
        
        saveEntitys(axisalignedbb);
        System.out.println("[JE] Saved " + entityOnShip.size() + " entities from ship");        
        
        if (dir != -2 && dir != -1) {
            messageToAllPlayersOnShip("Jumping in direction " + dir + " degrees to distance " + distance + " blocks ");
        } else if (dir == -1) {
            messageToAllPlayersOnShip("Jumping UP to distance " + distance + " blocks ");
        } else if (dir == -2) {
            messageToAllPlayersOnShip("Jumping DOWN to distance " + distance + " blocks ");
        }
        
        if (!betweenWorlds) {
            distance = getPossibleJumpDistance();
        } else {
            distance = 0;
        }
        
        if (distance <= this.shipLength && (!fromSpace && !toSpace)) {
            killEntity("Not enough space for jump.");
            messageToAllPlayersOnShip("Not enough space for jump!");
            return;
        }

        if (!checkForBedrockOnShip()) {
            killEntity("Is bedrock on the ship. Aborting.");
            messageToAllPlayersOnShip("Is bedrock on the ship. Aborting.");
            return;
        }

        int shipSize = getRealShipSize();
        saveShip(shipSize);
        removeShip();
        setBlocksUnderPlayers(false);
        
        isJumping = true;
        this.currentIndexInShip = 0;       
    }
    
    /**
     * Закончить прыжок: переместить Entity, разблокировать мир и удалить себя
     */
    public void finishJump() {
        moveEntitys(axisalignedbb, distance, dir, false);
        setBlocksUnderPlayers(true);
        
        // Разблокируем мир
        unlockWorlds();
        
        // Прыжок окончен
        killEntity("");
        on = false;        
    }
    
    /**
     * Удаление корабля из мира
     */
    public void removeShip() {
        for (JumpBlock jb : ship) {
            if (jb.blockTileEntity != null) {
                worldObj.removeBlockTileEntity(jb.x, jb.y, jb.z);
            }

            //System.out.println("[EJ] Removing block: " + jb.x + " " + jb.y + " " + jb.z + " " + jb.blockID);
            worldObj.setBlockAndMetadata(jb.x, jb.y, jb.z, 0, 0);
        }
    }

    /**
     * Сохранение корабля в память
     *
     * @param shipSize размер корабля (число блоков для перемещения)
     * @param deleteShip удалять ли блоки корабля сразу после сохранения
     */
    public void saveShip(int shipSize) {
        ship = new JumpBlock[shipSize];
        int index = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    if (ship == null) {
                        killEntity("ship is null!");
                        return;
                    }

                    int blockID = worldObj.getBlockId(x, y, z);
                    int blockMeta = worldObj.getBlockMetadata(x, y, z);
                    TileEntity tileentity = worldObj.getBlockTileEntity(x, y, z);

                    // Пустые блоки пропускаются
                    if (blockID == 0) {
                        continue;
                    }

                    if (tileentity != null) {
                        ship[index] = new JumpBlock(blockID, blockMeta, tileentity, x, y, z);
                    } else {
                        ship[index] = new JumpBlock(blockID, blockMeta, x, y, z);
                    }

                    index++;
                }
            }
        }

        System.out.println((new StringBuilder()).append("[JUMP] Ship saved: ").append((new StringBuilder()).append(ship.length).append(" blocks")).toString());
    }
    
    /**
     * Перемещение корабля
     */
    public void moveShip() {
        int blocksToMove = Math.min(BLOCKS_PER_TICK, ship.length - this.currentIndexInShip);
        
        System.out.println("[JE] Moving ship part: " + currentIndexInShip + "/" + ship.length + " [btm: " + blocksToMove + "]");
        
        // 1. Прыжок в космос
        if (toSpace) {
            for (int index = 0; index < blocksToMove; index++) {
                moveBlockToSpace(currentIndexInShip, distance, dir);
                this.currentIndexInShip++;
            }
        // 2. Прыжок из космоса
        } else if (fromSpace) {
            for (int index = 0; index < blocksToMove; index++) {
                moveBlockFromSpace(currentIndexInShip, distance, dir);
                this.currentIndexInShip++;
            }
        // 3. Обычный прыжок
        } else {            
            for (int index = 0; index < blocksToMove; index++) {
                moveBlock(currentIndexInShip, distance, dir);
                this.currentIndexInShip++;
            }
        }
    }

    /**
     * Проверка возможности прыжка и взрыв при столкновении
     *
     * @return Возвращает новую длинну прыжка (для стыковки), либо -1, если
     * прыжок невозможен в принципе
     */
    public int getPossibleJumpDistance() {
        int testDistance = this.distance;
        boolean canJump;

        int blowPoints = 0; // Вероятность взрыва ядра реактора

        while (true) {
            // Проверка, достаточно ли места в точке назначения прыжка
            canJump = checkMovement(testDistance);

            if (canJump) {
                break;
            }

            blowPoints++;
            testDistance--;
        }
        
        // Взрываем точку столкновения с препятствием, если это явно не стыковка или прыжок в вертикальной плоскости
        if (blowPoints > 5 && (this.dir != -1 && this.dir != -2)) {
            messageToAllPlayersOnShip(" [COLLISION] at (" + blowX + "; " + blowY + "; " + blowZ + ")");
            worldObj.createExplosion((Entity) null, blowX, blowY, blowZ, Math.min(4F * 30, 4F * (distance / 2)), true);
        }

        return testDistance;
    }

    /**
     * Проверка на наличие запрещённых блоков на корабле (бедрок)
     *
     * Применяется для предотвращения возможности зацепить варп-полем корабля
     * бедрок и оторвать его
     */
    public boolean checkForBedrockOnShip() {
        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    int blockID = worldObj.getBlockId(x, y, z);

                    // Пропускаем пустые блоки воздуха
                    if (blockID == 0) {
                        continue;
                    }

                    // Проверка блока
                    if (blockID == Block.bedrock.blockID) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * Получить реальное количество блоков, из которых состоит корабль 
     */
    public int getRealShipSize() {
        int shipSize = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    int blockID = worldObj.getBlockId(x, y, z);

                    // Пропускаем пустые блоки воздуха
                    if (blockID != 0) {
                        shipSize++;
                    }
                }
            }
        }

        return shipSize;
    }

    public void saveEntitys(AxisAlignedBB axisalignedbb) {
        this.entityOnShip = new ArrayList();
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
        
        for (Object o : list) {
            if (o == null || !(o instanceof Entity) || (o instanceof EntityJump)) {
                continue;
            }
            
            Entity entity = (Entity)o;

            MovingEntity movingentity = new MovingEntity(entity, MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
                        
            // Добавим в список Entity
            entityOnShip.add(movingentity);
        }
    }
    
    /**
     * Перемещение сущностей вместе с кораблем
     * @param axisalignedbb область корабля
     * @param distance расстояние перемещения
     * @param direction направление перемещения
     * @param restorePositions восстановление старых позиций для предотвращения выпадения, либо перемещение на новую
     * @return 
     */
    public boolean moveEntitys(AxisAlignedBB axisalignedbb, int distance, int direction, boolean restorePositions) {
        List list = this.entityOnShip;

        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof MovingEntity)) {
                    continue;
                }
                
                MovingEntity me = (MovingEntity)obj;
                
                Entity entity = me.entity;
                
                if (me == null) { continue; }
                
                int oldEntityX = me.oldX;
                int oldEntityY = me.oldY;
                int oldEntityZ = me.oldZ;

                int newEntityX, newEntityY, newEntityZ;

                newEntityX = oldEntityX;
                newEntityY = oldEntityY;
                newEntityZ = oldEntityZ;                
                
                if (!restorePositions && !toSpace) 
                {
                    if (!fromSpace) 
                    {
                        newEntityX = getNewXCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);
                        newEntityY = getNewYCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);
                        newEntityZ = getNewZCoord(oldEntityX, oldEntityY, oldEntityZ, distance, direction);    
                    } else {
                        newEntityX = oldEntityX;
                        newEntityY = 255 - this.shipDown - this.shipUp + oldEntityY - this.yCoord -1;
                        newEntityZ = oldEntityZ;                    
                    }
                }
                
                //System.out.println("Entity moving: old (" + oldEntityX + " " + oldEntityY + " " + oldEntityZ + ") -> new (" + newEntityX + " " + newEntityY + " " + newEntityZ);

                if (!(entity instanceof EntityPlayerMP)) {
                    entity.moveEntity(newEntityX, newEntityY, newEntityZ);
                    continue;
                }

                // Если на корабле есть кровать, то передвинуть точку спауна игрока
                ChunkCoordinates bedLocation = ((EntityPlayerMP) entity).getBedLocation();
                if (bedLocation != null && testBB(axisalignedbb, bedLocation.posX, bedLocation.posY, bedLocation.posZ)) {
                    bedLocation.posX = getNewXCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    bedLocation.posY = getNewYCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    bedLocation.posZ = getNewZCoord(bedLocation.posX, bedLocation.posY, bedLocation.posZ, distance, direction);
                    ((EntityPlayerMP) entity).setSpawnChunk(bedLocation, false);
                }

                ((EntityPlayerMP) entity).setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
                
                if (restorePositions) { continue; }
                
                if (toSpace) {
                    if (entity instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), WarpDrive.instance.spaceDimID, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)));
                        //((EntityPlayerMP) entity).setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
                        //if (!((EntityPlayerMP) entity).capabilities.isCreativeMode) {
                        //    ((EntityPlayerMP) entity).capabilities.allowFlying = true;
                        //}
                    } else {
                        entity.travelToDimension(WarpDrive.instance.spaceDimID);
                    }
                } else if (fromSpace) {
                    if (entity instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), 0, new SpaceTeleporter(DimensionManager.getWorld(0), 0, MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)));
                        //((EntityPlayerMP) entity).setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
                    } else {
                        entity.travelToDimension(0);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Проверка на вхождение точки в область (bounding-box)
     */
    public boolean testBB(AxisAlignedBB axisalignedbb, int x, int y, int z) {
        return axisalignedbb.minX <= (double) x && axisalignedbb.maxX >= (double) x && axisalignedbb.minY <= (double) y && axisalignedbb.maxY >= (double) y && axisalignedbb.minZ <= (double) z && axisalignedbb.maxZ >= (double) z;
    }

    /**
     * Получение новой координаты X (сдвиг)
     *
     * @param oldX старая координата X
     * @param oldY старая координата Y
     * @param oldZ старая координата Z
     * @param distance расстояние для перемещения
     * @param direction направление пермещения
     */
    public int getNewXCoord(int oldX, int oldY, int oldZ, int distance, int direction) {
        moveX = 0;
        moveZ = 0;
        // System.out.println("old: (" + oldX + "; " + oldZ + ") dis: " + distance + " dir: " + direction);
        int movementVector[] = getVector(direction);
        // System.out.println("Vector: (" + movementVector[0] + "; 0; " + movementVector[2]);

        moveX = movementVector[0] * distance;
        moveZ = movementVector[2] * distance;
        int result = oldX;

        if (direction != -1 && direction != -2) {
            result += moveX;
        }

        //System.out.println("moveX: " + moveX);

        return result;
    }

    /**
     * Получение новой координаты Y (сдвиг)
     *
     * @param oldX старая координата X
     * @param oldY старая координата Y
     * @param oldZ старая координата Z
     * @param distance расстояние для перемещения
     * @param direction направление пермещения
     */
    public int getNewYCoord(int i, int oldY, int k, int distance, int direction) {
        int result = oldY;

        if (direction == -1 || direction == -2) {
            if (direction == -1) {
                result += distance;
            } else {
                result -= distance;
            }
        }

        if (result >= 255) {
            result = 255;
        }

        if (result <= 0) {
            result = 3;
        }

        return result;
    }

    /**
     * Получение новой координаты Z (сдвиг)
     *
     * @param oldX старая координата X
     * @param oldY старая координата Y
     * @param oldZ старая координата Z
     * @param distance расстояние для перемещения
     * @param direction направление пермещения
     */
    public int getNewZCoord(int oldX, int oldY, int oldZ, int distance, int direction) {
        moveX = 0;
        moveZ = 0;
        // System.out.println("old: (" + oldX + "; " + oldZ + ") dis: " + distance + " dir: " + direction);
        int movementVector[] = getVector(direction);
        // System.out.println("Vector: (" + movementVector[0] + "; 0; " + movementVector[2]);
        moveX = movementVector[0] * distance;
        moveZ = movementVector[2] * distance;
        int result = oldZ;

        if (direction != -1 && direction != -2) {
            result += moveZ;
        }

        //System.out.println("moveZ: " + moveZ);

        return result;
    }

    public int rotsincos(int i, boolean flag) {
        // sin
        if (flag) {
            switch (i) {
                case 0:
                    return 0;

                case 90:
                    return 1;

                case 180:
                    return 0;

                case 270:
                    return -1;
            }
        } // cos
        else {
            switch (i) {
                case 0:
                    return 1;

                case 90:
                    return 0;

                case 180:
                    return -1;

                case 270:
                    return 0;
            }
        }

        return 0;
    }

    // Получение вектора в зависимости от направления прыжка
    // (3,14здец, конечно)
    public int[] getVector(int i) {
        int ai[] = {
            0, 0, 0
        };

        if (dz == 1) {
            switch (i) {
                case 0:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = 1;
                    break;

                case 90:
                    ai[0] = 1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 180:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = -1;
                    break;

                case 270:
                    ai[0] = -1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;
            }
        } else if (dz == -1) {
            switch (i) {
                case 0:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = -1;
                    break;

                case 90:
                    ai[0] = -1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 180:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = 1;
                    break;

                case 270:
                    ai[0] = 1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;
            }
        } else if (dx == 1) {
            switch (i) {
                case 0:
                    ai[0] = 1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 90:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = -1;
                    break;

                case 180:
                    ai[0] = -1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 270:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = 1;
                    break;
            }
        } else if (dx == -1) {
            switch (i) {
                case 0:
                    ai[0] = -1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 90:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = 1;
                    break;

                case 180:
                    ai[0] = 1;
                    ai[1] = 0;
                    ai[2] = 0;
                    break;

                case 270:
                    ai[0] = 0;
                    ai[1] = 0;
                    ai[2] = -1;
                    break;
            }
        }

        return ai;
    }

    /**
     * Проверка возможности установки корабля в месте, удалённом от корабля на
     * определённом расстоянии в сторону прыжка
     *
     * @param i
     * @return true, если корабль уместился на новом месте
     */
    public boolean checkMovement(int testDistance) {
        if ((dir == -1 && maxY + testDistance > 255) && !toSpace) {
            System.out.println("[JUMP] Reactor will blow due +high limit");
            return false;
        }

        if ((dir == -2 && minY - testDistance <= 8) && !fromSpace) {
            blowY = minY - testDistance;
            blowX = xCoord;
            blowZ = zCoord;

            System.out.println("[JUMP] Reactor will blow due -low limit");
            return false;
        }

        for (int y = minY; y <= maxY; y++) {
            for (int x = Xmin; x <= Xmax; x++) {
                for (int z = Zmin; z <= Zmax; z++) {
                    int newX = getNewXCoord(x, y, z, testDistance, dir);
                    int newY = getNewYCoord(x, y, z, testDistance, dir);
                    int newZ = getNewZCoord(x, y, z, testDistance, dir);

                    if (isBlockInShip(newX, newY, newZ)) {
                        continue;
                    }

                    int blockID = worldObj.getBlockId(newX, newY, newZ);
                    int blockOnShipID = worldObj.getBlockId(x, y, z);

                    if (blockOnShipID == Block.bedrock.blockID) {
                        return false;
                    }

                    if (blockOnShipID != 0 && blockID != 0 /*&& blockID != 9 && blockID != 8*/ && blockID != 18) {
                        blowX = x;
                        blowY = y;
                        blowZ = z;

                        System.out.println((new StringBuilder()).append("[JUMP] Reactor will blow due BlockID ").append((new StringBuilder()).append(blockID).append(" at (").append(newX).append(";").append(newY).append(";").append(newZ).append(")").toString()).toString());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Точка находится в варп-поле корабля?
     *
     * @param x
     * @param y
     * @param z
     * @return true, если находится
     */
    public boolean isBlockInShip(int x, int y, int z) {
        return x >= Xmin && x <= Xmax && y >= minY && y <= maxY && z >= Zmin && z <= Zmax;
    }

    public boolean moveBlock(int indexInShip, int distance, int direction) {
        return moveBlockSimple(indexInShip, distance, direction, false, false);
    }    

    /**
     * Перемещение одиночного блока из обычного мира в космос 
     * @param indexInShip
     * @param distance
     * @param direction
     * @return 
     */
    public boolean moveBlockToSpace(int indexInShip, int distance, int direction) {
        return moveBlockSimple(indexInShip, distance, direction, true, false);
    }
    
    /**
     * Перемещение одиночного блока из космоса в обычный мир
     * @param indexInShip
     * @param distance
     * @param direction
     * @return 
     */
    public boolean moveBlockFromSpace(int indexInShip, int distance, int direction) {
        return moveBlockSimple(indexInShip, distance, direction, false, true);
    }    
    
    /**
     * Перемещение одиночного блока на новое место
     *
     * @param indexInShip индекс блока в сохранённом в памяти корабле
     * @param distance расстояние для перемещения
     * @param direction направление перемещения
     * @return состояние перемещения
     */
    public boolean moveBlockSimple(int indexInShip, int distance, int direction, boolean toSpace, boolean fromSpace) {
        try {
            // OutOfBounds workaround
            if (indexInShip == ship.length) {
                indexInShip--;
            }
            
            JumpBlock shipBlock = ship[indexInShip];

            if (shipBlock == null) {
                return false;
            }

            int oldX = shipBlock.x;
            int oldY = shipBlock.y;
            int oldZ = shipBlock.z;
            
            World spaceWorld = DimensionManager.getWorld(WarpDrive.instance.spaceDimID);
            World surfaceWorld = DimensionManager.getWorld(0);
            int newY, newX, newZ;
            
            if (!toSpace && !fromSpace) 
            {
                newX = getNewXCoord(oldX, oldY, oldZ, distance, direction);
                newY = getNewYCoord(oldX, oldY, oldZ, distance, direction);
                newZ = getNewZCoord(oldX, oldY, oldZ, distance, direction);
            } else {
                // Если прыжок из космоса, то нужно поднять корабль до неба
                distance = 0;
                
                if (fromSpace) {
                    newY =  255 - this.shipDown - this.shipUp + oldY - this.yCoord -1;
                } else {
                    newY = oldY;
                }
                
                newX = oldX;
                newZ = oldZ;
            }
            
            int blockID = shipBlock.blockID;
            int blockMeta = shipBlock.blockMeta;

            if (!toSpace && !fromSpace)
            {
                worldObj.setBlockAndMetadataWithNotify(newX, newY, newZ, blockID, blockMeta);
            } else if (toSpace)
            {
                spaceWorld.setBlockAndMetadataWithNotify(newX, newY, newZ, blockID, blockMeta);
            } else if (fromSpace) {
                surfaceWorld.setBlockAndMetadataWithNotify(newX, newY, newZ, blockID, blockMeta);
            }

            NBTTagCompound oldnbt = new NBTTagCompound();


            if (shipBlock.blockTileEntity != null && blockID != 159 && blockID != 149 && blockID != 156 && blockID != 146 && blockID != 145) {
                shipBlock.blockTileEntity.writeToNBT(oldnbt);
                TileEntity newTileEntity = null;
                
                if (!toSpace && !fromSpace) {
                    newTileEntity = worldObj.getBlockTileEntity(newX, newY, newZ);     
                } else if (toSpace) {
                    newTileEntity = spaceWorld.getBlockTileEntity(newX, newY, newZ); 
                } else if (fromSpace) {
                    newTileEntity = surfaceWorld.getBlockTileEntity(newX, newY, newZ); 
                }
                   
                             
                if (newTileEntity == null) {
                    return false; // PIZDEC!!!
                }
                newTileEntity.readFromNBT(oldnbt);
                if (!toSpace && !fromSpace)
                {
                    worldObj.setBlockTileEntity(newX, newY, newZ, newTileEntity);
                } else if (toSpace)
                {
                    //newTileEntity.worldObj = spaceWorld;
                    spaceWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);
                } else if (fromSpace) {
                    //newTileEntity.worldObj = surfaceWorld;
                    surfaceWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);                    
                }
                
                worldObj.removeBlockTileEntity(oldX, oldY, oldZ);             
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
    }

    @Override
    protected void entityInit() {
        //onUpdate();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1) {
    }
    /*
     @Override
     protected void entityInit() {
     throw new UnsupportedOperationException("Not supported yet.");
     }

     @Override
     protected void readEntityFromNBT(NBTTagCompound var1) {
     throw new UnsupportedOperationException("Not supported yet.");
     }

     @Override
     protected void writeEntityToNBT(NBTTagCompound var1) {
     throw new UnsupportedOperationException("Not supported yet.");
     }*/
}