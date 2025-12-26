package com.stellarcolonizer.model.economy;

import com.stellarcolonizer.model.faction.PlayerFaction;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import java.util.HashMap;
import java.util.Map;

/**
 * 宇宙资源市场 - 玩家可以用金钱购买资源或出售资源
 */
public class UniversalResourceMarket {
    private PlayerFaction playerFaction;
    // 基础价格表，每个资源类型的初始价格
    private final Map<ResourceType, Double> basePrices;
    // 当前价格倍数表，基于交易量调整
    private final Map<ResourceType, Double> currentPriceMultipliers;
    // 记录每种资源的交易量，用于动态定价
    private final Map<ResourceType, Integer> transactionVolumes;
    
    public UniversalResourceMarket(PlayerFaction player) {
        this.playerFaction = player;
        basePrices = new HashMap<>();
        currentPriceMultipliers = new HashMap<>();
        transactionVolumes = new HashMap<>();
        
        // 设置每种资源的基础价格
        for (ResourceType type : ResourceType.values()) {
            // 根据资源稀有度设置基础价格
            switch (type) {
                case FOOD:
                    basePrices.put(type, 1.0); // 食物相对便宜
                    break;
                case ENERGY:
                    basePrices.put(type, 1.2); // 能量略贵
                    break;
                case METAL:
                    basePrices.put(type, 1.5); // 金属较贵
                    break;
                case FUEL:
                    basePrices.put(type, 2.0); // 燃料更贵
                    break;
                case SCIENCE:
                    basePrices.put(type, 3.0); // 科研昂贵
                    break;
                case MONEY:
                    basePrices.put(type, 0.0); // 金钱不能买卖
                    break;
                case EXOTIC_MATTER:
                    basePrices.put(type, 10.0); // 稀有资源非常昂贵
                    break;
                case NEUTRONIUM:
                    basePrices.put(type, 12.0);
                    break;
                case CRYSTAL:
                    basePrices.put(type, 8.0);
                    break;
                case DARK_MATTER:
                    basePrices.put(type, 15.0);
                    break;
                case ANTI_MATTER:
                    basePrices.put(type, 20.0);
                    break;
                case LIVING_METAL:
                    basePrices.put(type, 18.0);
                    break;
                default:
                    basePrices.put(type, 1.0);
                    break;
            }
            
            // 初始价格倍数为1.0（即正常价格）
            currentPriceMultipliers.put(type, 1.0);
            // 初始交易量为0
            transactionVolumes.put(type, 0);
        }
        
        // 金钱不能交易，设置为0
        currentPriceMultipliers.put(ResourceType.MONEY, 0.0);
    }
    
    // 保留无参数构造函数用于向后兼容，但不推荐使用
    @Deprecated
    public UniversalResourceMarket() {
        this(null); // 调用带参数构造函数，但playerFaction为null
    }
    
    /**
     * 购买资源
     * @param resourceType 要购买的资源类型
     * @param quantity 购买数量
     * @return 需要支付的金额
     */
    public double getPurchaseCost(ResourceType resourceType, int quantity) {
        if (resourceType == ResourceType.MONEY) {
            return 0; // 不能买卖金钱
        }
        
        double basePrice = basePrices.get(resourceType);
        double currentMultiplier = currentPriceMultipliers.get(resourceType);
        
        // 计算总成本（按当前价格）
        double totalCost = basePrice * currentMultiplier * quantity;
        
        return totalCost;
    }
    
    /**
     * 出售资源
     * @param resourceType 要出售的资源类型
     * @param quantity 出售数量
     * @return 可以获得的金额
     */
    public double getSaleRevenue(ResourceType resourceType, int quantity) {
        if (resourceType == ResourceType.MONEY) {
            return 0; // 不能买卖金钱
        }
        
        double basePrice = basePrices.get(resourceType);
        double currentMultiplier = currentPriceMultipliers.get(resourceType);
        
        // 出售价格是购买价格的一定比例（比如70%）
        double saleMultiplier = currentMultiplier * 0.7;
        
        // 计算总收入
        double totalRevenue = basePrice * saleMultiplier * quantity;
        
        return totalRevenue;
    }
    
    /**
     * 检查玩家是否有足够金钱购买资源
     * @param resourceType 资源类型
     * @param quantity 购买数量
     * @return 是否足够金钱
     */
    public boolean canAfford(ResourceType resourceType, int quantity) {
        if (playerFaction == null) return false;
        double cost = getPurchaseCost(resourceType, quantity);
        return playerFaction.getResourceStockpile().getResource(ResourceType.MONEY) >= cost;
    }
    
    /**
     * 检查玩家是否有足够资源出售
     * @param resourceType 资源类型
     * @param quantity 出售数量
     * @return 是否足够资源
     */
    public boolean hasEnoughResource(ResourceType resourceType, int quantity) {
        if (playerFaction == null) return false;
        return playerFaction.getResourceStockpile().getResource(resourceType) >= quantity;
    }
    
    /**
     * 购买资源
     * @param resourceType 要购买的资源类型
     * @param quantity 购买数量
     * @return 是否购买成功
     */
    public boolean buyResource(ResourceType resourceType, int quantity) {
        if (resourceType == ResourceType.MONEY) {
            return false; // 不能买卖金钱
        }
        
        double cost = getPurchaseCost(resourceType, quantity);
        ResourceStockpile playerStock = playerFaction.getResourceStockpile();
        
        // 检查金钱是否足够
        if (playerStock.getResource(ResourceType.MONEY) >= cost) {
            // 扣钱
            playerStock.consumeResource(ResourceType.MONEY, (float)cost);
            // 增加资源
            playerStock.addResource(resourceType, (float)quantity);
            
            // 更新交易量（购买增加需求，导致价格上涨）
            int currentVolume = transactionVolumes.get(resourceType);
            transactionVolumes.put(resourceType, currentVolume + quantity);
            
            // 更新价格倍数（每购买1单位，价格增加2%）
            double newMultiplier = 1.0 + (transactionVolumes.get(resourceType) * 0.02);
            // 设置最大价格倍数限制，防止价格过高
            newMultiplier = Math.min(newMultiplier, 5.0); 
            currentPriceMultipliers.put(resourceType, newMultiplier);
            
            return true;
        }
        return false;
    }
    
    /**
     * 出售资源
     * @param resourceType 要出售的资源类型
     * @param quantity 出售数量
     * @return 是否出售成功
     */
    public boolean sellResource(ResourceType resourceType, int quantity) {
        if (resourceType == ResourceType.MONEY) {
            return false; // 不能买卖金钱
        }
        
        double revenue = getSaleRevenue(resourceType, quantity);
        ResourceStockpile playerStock = playerFaction.getResourceStockpile();
        
        // 检查资源是否足够
        if (playerStock.getResource(resourceType) >= quantity) {
            // 扣资源
            playerStock.consumeResource(resourceType, (float)quantity);
            // 加钱
            playerStock.addResource(ResourceType.MONEY, (float)revenue);
            
            // 更新交易量（出售增加供应，导致价格下降）
            int currentVolume = transactionVolumes.get(resourceType);
            int newVolume = Math.max(0, currentVolume - quantity); // 防止负数
            transactionVolumes.put(resourceType, newVolume);
            
            // 更新价格倍数（每出售1单位，价格减少1%，但不低于0.5倍）
            double newMultiplier = 1.0 - (quantity * 0.01);
            newMultiplier = Math.max(newMultiplier, 0.5); 
            // 应用到当前基础价格上，但不能低于0.5倍
            double currentMultiplier = currentPriceMultipliers.get(resourceType);
            double finalMultiplier = Math.max(currentMultiplier * newMultiplier, 0.5);
            currentPriceMultipliers.put(resourceType, finalMultiplier);
            
            return true;
        }
        return false;
    }
    
    /**
     * 获取当前玩家金钱
     */
    public float getPlayerMoney() {
        return playerFaction.getResourceStockpile().getResource(ResourceType.MONEY);
    }
    
    /**
     * 执行购买操作（保留原方法用于内部计算）
     * @param resourceType 资源类型
     * @param quantity 购买数量
     * @return 实际花费金额
     */
    public double executePurchase(ResourceType resourceType, int quantity) {
        if (resourceType == ResourceType.MONEY) {
            return 0; // 不能买卖金钱
        }
        
        double cost = getPurchaseCost(resourceType, quantity);
        
        // 更新交易量（购买增加需求，导致价格上涨）
        int currentVolume = transactionVolumes.get(resourceType);
        transactionVolumes.put(resourceType, currentVolume + quantity);
        
        // 更新价格倍数（每购买1单位，价格增加2%）
        double newMultiplier = 1.0 + (transactionVolumes.get(resourceType) * 0.02);
        // 设置最大价格倍数限制，防止价格过高
        newMultiplier = Math.min(newMultiplier, 5.0); 
        currentPriceMultipliers.put(resourceType, newMultiplier);
        
        return cost;
    }
    
    /**
     * 执行出售操作（保留原方法用于内部计算）
     * @param resourceType 资源类型
     * @param quantity 出售数量
     * @return 实际获得金额
     */
    public double executeSale(ResourceType resourceType, int quantity) {
        if (resourceType == ResourceType.MONEY) {
            return 0; // 不能买卖金钱
        }
        
        double revenue = getSaleRevenue(resourceType, quantity);
        
        // 更新交易量（出售增加供应，导致价格下降）
        int currentVolume = transactionVolumes.get(resourceType);
        int newVolume = Math.max(0, currentVolume - quantity); // 防止负数
        transactionVolumes.put(resourceType, newVolume);
        
        // 更新价格倍数（每出售1单位，价格减少1%，但不低于0.5倍）
        double newMultiplier = 1.0 - (quantity * 0.01);
        newMultiplier = Math.max(newMultiplier, 0.5); 
        // 应用到当前基础价格上，但不能低于0.5倍
        double currentMultiplier = currentPriceMultipliers.get(resourceType);
        double finalMultiplier = Math.max(currentMultiplier * newMultiplier, 0.5);
        currentPriceMultipliers.put(resourceType, finalMultiplier);
        
        return revenue;
    }
    
    /**
     * 获取当前价格倍数
     */
    public double getCurrentPriceMultiplier(ResourceType resourceType) {
        return currentPriceMultipliers.get(resourceType);
    }
    
    /**
     * 获取基础价格
     */
    public double getBasePrice(ResourceType resourceType) {
        return basePrices.get(resourceType);
    }
    
    /**
     * 获取当前总价格（基础价格*倍数）
     */
    public double getCurrentPrice(ResourceType resourceType) {
        return basePrices.get(resourceType) * currentPriceMultipliers.get(resourceType);
    }
}